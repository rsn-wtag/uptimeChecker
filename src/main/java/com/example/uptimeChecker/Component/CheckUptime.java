package com.example.uptimeChecker.Component;

import com.example.uptimeChecker.DTO.UptimeStatusDTO;
import com.example.uptimeChecker.Service.WebsiteService;
import com.example.uptimeChecker.DTO.WebsiteDetailsDTO;
import com.example.uptimeChecker.Service.DowntimeService;
import com.example.uptimeChecker.Service.EmailNotificationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.*;


@Component
public class CheckUptime {
    private static Map<Integer, WebsiteDetailsDTO> scheduledTasksMap = new ConcurrentHashMap<>();
    private static Set<Integer> tasksToBeCanceled= new HashSet<>();
    private List<WebsiteDetailsDTO> urlList = new ArrayList<>();
    private CustomThreadPoolTaskScheduler schedulerExecutorService;
    /*
     * this executor service is responsible to reschedule task and also schedule task for all website at application init
     * */
    private ExecutorService reschedulerExecutorService;
    @Autowired
    private SimpMessagingTemplate brokerMessagingTemplate;
    @Autowired
    private WebsiteService websiteService;
    @Autowired
    private DowntimeService downtimeService;

    @Autowired
    private EmailNotificationService emailNotificationService;
    @Autowired
    DowntimeSummaryManagementComponent downtimeSummaryManagementComponent;
    private Integer schedulerPeriod;
    private Integer maxFailCount;

    private  TimeUnit timeUnit;
    public CheckUptime(  @Value("${rescheduler.poolsize}") String reschedulerPoolSize,
                         @Value("${scheduler.poolsize}") String schedulerPoolSize,
                         @Value("${scheduler.period}") String  schedulerPeriod,
                         @Value("${max.fail.count}" ) String maxFailCount){
         schedulerExecutorService = new CustomThreadPoolTaskScheduler(Integer.parseInt(schedulerPoolSize));
         reschedulerExecutorService = Executors.newFixedThreadPool(Integer.parseInt(reschedulerPoolSize));
         this.schedulerPeriod=Integer.parseInt(schedulerPeriod);
         this.maxFailCount=Integer.parseInt(maxFailCount);
         this.timeUnit=TimeUnit.SECONDS;
    }

    //region public method
    @PostConstruct
    public void init() {
        try {
            if(downtimeSummaryManagementComponent.executeEndOfDayTaskForMissedScheduler()) {
                //if the schedule is missed that method will update the summary table,
                // so no need to update end time in downtime table
                updateEndOfDowntime();
            }
            broadcastWebsiteUptimeData();
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
        if(!tasksToBeCanceled.contains(websiteDetailsDTO.getWebId()))
        reschedulerExecutorService.execute(() -> {
            int delay = calculateBackOffTime(websiteDetailsDTO.getFailCount());
            System.out.println(delay + "delay");
            schedulerExecutorService.scheduleAtFixedRate(() -> {
                checkAvailability(websiteDetailsDTO);
            }, delay, this.schedulerPeriod, timeUnit, websiteDetailsDTO.getWebId(), websiteDetailsDTO.getUrl());
        });

    }

    public void cancelTask(Integer webId){
        WebsiteDetailsDTO websiteDetailsDTO= scheduledTasksMap.get(webId);
       if(websiteDetailsDTO !=null){
           //if website is schduled for check up then simply cancel it
           websiteDetailsDTO.getFuture().cancel(false);
       }else{
           //else add it to tasksToBeCanceled set for rescheduler to know this website should not be rescheduled
           tasksToBeCanceled.add(webId);
      }
    }
    //endregion

    //region private
    private void broadcastWebsiteUptimeData() throws InterruptedException {
        getUserRegisteredWebsiteList();
        for (int i = 0; i < urlList.size(); i++) {
            addForExecution(urlList.get(i));
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
                System.out.println("connection up......" + websiteDetailsDTO.getUrl());
                WebsiteDetailsDTO websiteDetailsDTOFromMap= scheduledTasksMap.get(websiteDetailsDTO.getWebId());
                if ( websiteDetailsDTOFromMap != null) {
                    if(websiteDetailsDTOFromMap.getTotalConsecutiveFailCount()>0){
                        //updating Downtime end time value when website is up again after failure
                        downtimeService.updateDowntimeInfo(websiteDetailsDTOFromMap);
                    }

                    websiteDetailsDTOFromMap.setFailCount(0);// resetting fail count
                    websiteDetailsDTOFromMap.setTotalConsecutiveFailCount(0);//resetting consecutive total fail count

                    System.out.println(scheduledTasksMap.get(websiteDetailsDTO.getWebId()).getFuture().getDelay(timeUnit));
                }
                broadcastData(websiteDetailsDTO, false);
                return true;
            } else {
                System.out.println("connection down....");
                reschedule(websiteDetailsDTO.getWebId());
                broadcastData( websiteDetailsDTO, true);

                return false;
            }


        } catch (Exception e) {
            System.out.println("in exception..." + websiteDetailsDTO.getUrl());
            reschedule(websiteDetailsDTO.getWebId());
            try {
                broadcastData(websiteDetailsDTO,true);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }

            // throw new RuntimeException(e);
        }
        return true;
    }

    private void reschedule(Integer websiteId) {
        if (scheduledTasksMap.get(websiteId) != null) {
            WebsiteDetailsDTO websiteDetailsDTOFromMap = scheduledTasksMap.get(websiteId);

            ScheduledFuture<?> future = websiteDetailsDTOFromMap.getFuture();
            Integer prevFailCount = websiteDetailsDTOFromMap.getFailCount();
            Integer prevTotalConsecutiveFailCount = websiteDetailsDTOFromMap.getTotalConsecutiveFailCount();
            websiteDetailsDTOFromMap.setTotalConsecutiveFailCount(prevTotalConsecutiveFailCount + 1);
            websiteDetailsDTOFromMap.setFailCount(prevFailCount + 1);
            future.cancel(false);

            try {
                WebsiteDetailsDTO websiteDetailsDTO= (WebsiteDetailsDTO) websiteDetailsDTOFromMap.clone();
                //if fail count reached max fail count we assume it to be  down
                if(websiteDetailsDTOFromMap.getFailCount()>=maxFailCount){
                    websiteDetailsDTOFromMap.setFailCount(0);
                    websiteDetailsDTO.setFailCount(0);
                    downtimeService.saveDowntimeInfo(websiteDetailsDTO);
                    emailNotificationService.sendMessageToUsers(websiteDetailsDTO);
                }

                addForExecution(websiteDetailsDTO);

            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
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
            failCount=failCount-1;
            return (int) (schedulerPeriod*Math.pow(2,failCount));
        }

    }



    private void broadcastData(WebsiteDetailsDTO websiteDetailsDTO, boolean isDown) throws InterruptedException {
        String destination="/dashboard/data/"+websiteDetailsDTO.getWebId();
        UptimeStatusDTO uptimeStatusDTO= new UptimeStatusDTO(isDown,websiteDetailsDTO.getUrl(), new Date(), websiteDetailsDTO.getWebId());
        ObjectMapper mapper =  JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();
        String payload="";
        try {
             payload= mapper.writeValueAsString(uptimeStatusDTO);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        this.brokerMessagingTemplate.convertAndSend(destination, payload);
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
                WebsiteDetailsDTO websiteDetailsDTO = new WebsiteDetailsDTO(id, url, future, 0,0);
                scheduledTasksMap.put(id, websiteDetailsDTO);
            } else {
                scheduledTasksMap.get(id).setFuture(future);
            }


        }
    }
    //endregion
}
