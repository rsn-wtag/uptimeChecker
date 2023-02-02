package com.example.uptimeChecker.Component;

import com.example.uptimeChecker.DTO.UptimeStatusDTO;
import com.example.uptimeChecker.Entities.Downtime;
import com.example.uptimeChecker.Service.NotificationServiceImpl;
import com.example.uptimeChecker.Service.WebsiteService;
import com.example.uptimeChecker.DTO.WebsiteDetailsDTO;
import com.example.uptimeChecker.Service.DowntimeService;
import com.example.uptimeChecker.Service.NotificationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;


@Component
public class CheckUptime {
    private static final Logger logger = LoggerFactory.getLogger(CheckUptime.class);

    private static final Map<Integer, WebsiteDetailsDTO> scheduledTasksMap = new ConcurrentHashMap<>();
    private static final Set<Integer> tasksToBeCanceled= new HashSet<>();
    private List<WebsiteDetailsDTO> urlList = new ArrayList<>();
    private CustomThreadPoolTaskScheduler checkWebsiteSchedulerExecutorService;
    /*
     * this executor service is responsible to reschedule task and also schedule task for all website at application init
     * */
    private ExecutorService sentToSchedulerExecutorService;
    @Autowired
    private SimpMessagingTemplate brokerMessagingTemplate;
    @Autowired
    private WebsiteService websiteService;
    @Autowired
    private DowntimeService downtimeService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    DowntimeSummaryManagementComponent downtimeSummaryManagementComponent;
    private final  Integer schedulerPeriod;
    private final Integer maxFailCount;

    private  final TimeUnit timeUnit;
    public CheckUptime(  @Value("${rescheduler.poolsize}") String reschedulerPoolSize,
                         @Value("${scheduler.poolsize}") String schedulerPoolSize,
                         @Value("${scheduler.period}") String  schedulerPeriod,
                         @Value("${max.fail.count}" ) String maxFailCount){
         checkWebsiteSchedulerExecutorService = new CustomThreadPoolTaskScheduler(Integer.parseInt(schedulerPoolSize));
         sentToSchedulerExecutorService = Executors.newFixedThreadPool(Integer.parseInt(reschedulerPoolSize));
         this.schedulerPeriod=Integer.parseInt(schedulerPeriod);
         this.maxFailCount=Integer.parseInt(maxFailCount);
         this.timeUnit=TimeUnit.SECONDS;
    }

    //region public method
    @PostConstruct
    public void init() {
        try {
            if(websiteService.isAnyWebsiteExists()){
                if(!downtimeSummaryManagementComponent.executeEndOfDayTaskForMissedScheduler()) {
                    //if the schedule is missed that method will update the summary table,
                    // so no need to update end time in downtime table
                    updateEndOfDowntime();
                }
                scheduleWebsiteUptimeCheckTasks();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    //At Application startup. check if any row exist where end time is not updated.
    //if found then update the end time assuming that the website was up when our application was down.
    //calculates approx downtime from the fail count and update end time.
    private void updateEndOfDowntime() {
        downtimeService.updateEndOfDowntime(timeUnit,schedulerPeriod);
    }

    /*
    * This method is responsible for scheduling website for uptime tracking
    * */
    public void addForExecution(WebsiteDetailsDTO websiteDetailsDTO) {
        //if tasksToBeCanceled contains the webId that means the website is removed from the system. so, we won't reschedule it
        if(!tasksToBeCanceled.contains(websiteDetailsDTO.getWebId())){
            sentToSchedulerExecutorService.execute(() -> {
                int delay = calculateBackOffTime(websiteDetailsDTO.getFailCount());
                System.out.println(delay + "delay");
                checkWebsiteSchedulerExecutorService.scheduleAtFixedRate(() -> {
                    checkAvailability(websiteDetailsDTO);
                }, delay, this.schedulerPeriod, timeUnit, websiteDetailsDTO.getWebId(), websiteDetailsDTO.getUrl());
            });
        }else{
            //remove from tasksToBeCanceled as its already canceled and also have been saved from being rescheduled.
            tasksToBeCanceled.remove(websiteDetailsDTO.getWebId());
        }

    }

    public void cancelTask(Integer webId){
        WebsiteDetailsDTO websiteDetailsDTO= scheduledTasksMap.get(webId);
       if(websiteDetailsDTO !=null){
          if( !websiteDetailsDTO.getFuture().isCancelled()){
              //if website is scheduled for check up and not canceled for reschedule then simply cancel it
              websiteDetailsDTO.getFuture().cancel(false);
          }
           scheduledTasksMap.remove(webId);
       }
       //after all this caution if the website reaches addForExecution method,
       // then to restrict that website to be again scheduled, add it to tasksToBeCanceled queue
        tasksToBeCanceled.add(webId);
    }
    //endregion

    //region private
    private void scheduleWebsiteUptimeCheckTasks() throws InterruptedException {
        getUserRegisteredWebsiteList();
        for (WebsiteDetailsDTO websiteDetailsDTO : urlList) {
            addForExecution(websiteDetailsDTO);
        }
    }
    /*
    * check if the website is up. if website is down then save to DB.
    * update endtime of downtime when website is again up.
    * */
    private boolean checkAvailability(WebsiteDetailsDTO websiteDetailsDTO) {
        try {
            System.out.println("at start..." + websiteDetailsDTO.getUrl());
            HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(websiteDetailsDTO.getUrl()).openConnection(); //return urlconnection superclass of httpconnection
            httpURLConnection.connect();
            httpURLConnection.setConnectTimeout(1000);
            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode >= 200 && responseCode < 400) {
                onWebsiteUp(websiteDetailsDTO);
                return true;
            } else {
                onWebsiteDown(websiteDetailsDTO);
                return false;
            }

        } catch (Exception e) {
            System.out.println("in exception..." + websiteDetailsDTO.getUrl());
            try {
                onWebsiteDown(websiteDetailsDTO);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }

            // throw new RuntimeException(e);
        }
        return true;
    }



    private void onWebsiteUp(WebsiteDetailsDTO websiteDetailsDTO) throws InterruptedException {
        System.out.println("connection up......" + websiteDetailsDTO.getUrl());
        WebsiteDetailsDTO websiteDetailsDTOFromMap= scheduledTasksMap.get(websiteDetailsDTO.getWebId());
        if ( websiteDetailsDTOFromMap != null) {
            if(websiteDetailsDTOFromMap.getTotalConsecutiveFailCount()>maxFailCount){
                //updating Downtime end time value when website is up again after failure
                Downtime downtime= downtimeService.updateDowntimeInfo(websiteDetailsDTOFromMap);
                websiteDetailsDTOFromMap.setDowntimeId(downtime.getDownTimeId());
            }

            websiteDetailsDTOFromMap.setFailCount(0);// resetting fail count
            websiteDetailsDTOFromMap.setTotalConsecutiveFailCount(0);//resetting consecutive total fail count

            System.out.println(scheduledTasksMap.get(websiteDetailsDTO.getWebId()).getFuture().getDelay(timeUnit));
        }
        broadcastData(websiteDetailsDTO, false);
    }


    private void onWebsiteDown(WebsiteDetailsDTO websiteDetailsDTO) throws InterruptedException {
        if (scheduledTasksMap.get(websiteDetailsDTO.getWebId()) != null) {
            WebsiteDetailsDTO websiteDetailsDTOFromMap = scheduledTasksMap.get(websiteDetailsDTO.getWebId());
            Integer prevFailCount = websiteDetailsDTOFromMap.getFailCount();
            Integer prevTotalConsecutiveFailCount = websiteDetailsDTOFromMap.getTotalConsecutiveFailCount();
            websiteDetailsDTOFromMap.setTotalConsecutiveFailCount(prevTotalConsecutiveFailCount + 1);
            websiteDetailsDTOFromMap.setFailCount(prevFailCount + 1);
            try {
                websiteDetailsDTO= (WebsiteDetailsDTO) websiteDetailsDTOFromMap.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
            //Save downtime info and Send notification only once when consecutive fail count reaches max fail count
            //then we assume the website is down
            if(websiteDetailsDTOFromMap.getTotalConsecutiveFailCount()==maxFailCount){
                notificationService.sendMessageToUsers(websiteDetailsDTO);
            }
            //when consecutive fail count exceed the max fail count broadcast down notification(websocket)
            if(websiteDetailsDTOFromMap.getTotalConsecutiveFailCount()>=maxFailCount){
                broadcastData( websiteDetailsDTO, true);
            }
            //if fail count reached max fail count we assume it to be  down, and we reset the fail count
            if(websiteDetailsDTOFromMap.getFailCount()==maxFailCount){
                websiteDetailsDTOFromMap.setFailCount(0);
                Downtime downtime= downtimeService.saveDowntimeInfo(websiteDetailsDTO);
                websiteDetailsDTOFromMap.setDowntimeId(downtime.getDownTimeId());
            }
            //after updating all required parameters of map item call the reschedule
            reschedule(websiteDetailsDTO.getWebId());
        }

    }


    private void reschedule(Integer websiteId) {
        if (scheduledTasksMap.get(websiteId) != null) {
            WebsiteDetailsDTO websiteDetailsDTOFromMap = scheduledTasksMap.get(websiteId);

            ScheduledFuture<?> future = websiteDetailsDTOFromMap.getFuture();
            if(!future.isCancelled()){
                future.cancel(false);

                try {
                    WebsiteDetailsDTO websiteDetailsDTO= (WebsiteDetailsDTO) websiteDetailsDTOFromMap.clone();

                    addForExecution(websiteDetailsDTO);

                } catch (CloneNotSupportedException e) {
                    throw new RuntimeException(e);
                }
            }

        }
    }


    private void broadcastData(WebsiteDetailsDTO websiteDetailsDTO, boolean isDown)  {
        String destination="/dashboard/data/"+websiteDetailsDTO.getWebId();
        UptimeStatusDTO uptimeStatusDTO= new UptimeStatusDTO(isDown,websiteDetailsDTO.getUrl(), new Date(), websiteDetailsDTO.getWebId());
        ObjectMapper mapper =  JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();
        String payload;
        try {
             payload= mapper.writeValueAsString(uptimeStatusDTO);
            this.brokerMessagingTemplate.convertAndSend(destination, payload);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            logger.error("Unable to broad cast uptime info");
        }

    }


    /*
     *Fetch All registered website info From DB. called at application startup.
     *  */
    private void getUserRegisteredWebsiteList() {
        urlList = websiteService.getWesiteDetailList();
    }


    private Integer calculateBackOffTime(int failCount) {
        if (failCount == 0) return 0;
        else{
           // failCount=failCount-1;
            return (int) (schedulerPeriod*Math.pow(2,failCount));
        }

    }
    //endregion

    //region innerClass
    /*
     * CustomerSchedular to save SheduleFuture im schdulerMap for future rescheduling
     * */
    public static class CustomThreadPoolTaskScheduler extends ScheduledThreadPoolExecutor {

        public CustomThreadPoolTaskScheduler(int corePoolSize) {
            super(corePoolSize);
        }


        public void scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit, Integer id, String url) {
            ScheduledFuture<?> future = super.scheduleAtFixedRate(command, initialDelay, period, unit);

            if (scheduledTasksMap.get(id) == null) {
                WebsiteDetailsDTO websiteDetailsDTO = new WebsiteDetailsDTO(id, url, future);
                scheduledTasksMap.put(id, websiteDetailsDTO);
            } else {
                scheduledTasksMap.get(id).setFuture(future);
            }


        }
    }
    //endregion
}
