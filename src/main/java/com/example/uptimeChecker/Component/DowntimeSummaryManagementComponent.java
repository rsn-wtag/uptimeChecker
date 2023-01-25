package com.example.uptimeChecker.Component;

import com.example.uptimeChecker.DTO.WebsiteDetailsDTO;
import com.example.uptimeChecker.Entities.*;
import com.example.uptimeChecker.Repositories.DowntimeRepository;
import com.example.uptimeChecker.Repositories.DowntimeSummaryRepository;
import com.example.uptimeChecker.Service.WebsiteService;
import com.example.uptimeChecker.security.jwt.AuthEntryPointJwt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Component
@EnableScheduling
@Transactional
public class DowntimeSummaryManagementComponent {
    private static final Logger logger = LoggerFactory.getLogger(AuthEntryPointJwt.class);
    @Autowired
    DowntimeSummaryRepository downtimeSummaryRepository;

    @Autowired
    DowntimeRepository downtimeRepository;
    @Autowired
    WebsiteService websiteService;

    //todo: what if application was down at that time
    //this whole task should be executed at db level with procedure, that would be better I think
    @Scheduled(cron = "0 0 0 * * *")//this method will be executed every day at 0:0:0
//  @Scheduled(cron = "0 33 10 * * *")//test
    public void executeEndOfDayTask(){
      Calendar c= Calendar.getInstance();
      Date nextDate= c.getTime();
      c.add(Calendar.DATE, -1);
      Date currentDate=  c.getTime();
      OffsetTime todayEndTime=OffsetTime.now().withHour(23).withMinute(59).withSecond(59);
      OffsetTime nextDayStartTime=OffsetTime.now().withHour(0).withMinute(0).withSecond(0);
        //get all of them whose end time is not yet updated.
        //update their end time. and insert another row with tomorrow  and start time as 00:00:00
        Set<Downtime> downtimeSet=  downtimeRepository.findByDateAndEndTimeGreaterThanStartTime(currentDate);
        for(Downtime downtime:downtimeSet){
            downtime= downtimeRepository.findById(downtime.getDownTimeId()).get();
            //re checking if end of  downtime is updated or not
            if(downtime.getEndTime()==null){
                downtime.setEndTime(todayEndTime);
                downtimeRepository.save(downtime);
                Downtime newDowntime= new Downtime(downtime.getWebId(), nextDayStartTime,nextDate,0);
                downtimeRepository.save(newDowntime);
            }
        }

      List<WebsiteDetailsDTO> websiteDetailsDTOList=  websiteService.getWesiteDetailList();
      for(WebsiteDetailsDTO websiteDetailsDTO:websiteDetailsDTOList){
          try {
              //for all website calculate and save summary in the summary table
              calculateAndSaveDowntimeSummary(websiteDetailsDTO.getWebId(),currentDate);
          }catch (Exception e){
                logger.error("Error Occured at summary Calculation/data deletion for WebID "+websiteDetailsDTO.getWebId()+", date: "+currentDate);
          }
      }
    }


    void calculateAndSaveDowntimeSummary(Integer webId, Date date){
      Set<Downtime> DowntimeSet= downtimeRepository.findByDateAndWebId(date, webId);
      long downtimeInSecond =0;
          //calculating total downtime
          for(Downtime downtime:DowntimeSet){
              if(downtime.getEndTime()!=null){
                  downtimeInSecond= ((downtime.getEndTime().getSecond() - downtime.getStartTime().getSecond()))+ downtimeInSecond;
              }
          }
        DowntimeSummary downtimeSummary= new DowntimeSummary();
        downtimeSummary.setDowntimeSummaryPk(new DowntimeSummary_PK(date,webId));
        downtimeSummary.setTotalDownTime(downtimeInSecond);
        downtimeSummary.setUptimePercentage(calculateUptimePercentage(downtimeInSecond));
        downtimeSummaryRepository.save(downtimeSummary);
        //delete all data for that day for that specific website.
        deletePreviousDayDate(webId,date);
    }

    private void deletePreviousDayDate(Integer webId, Date date){
        downtimeRepository.deleteByWebIdAndDate(webId, date);
    }
    private double calculateUptimePercentage(long downtimeInSecond){
        long totalSecondsInADay = 86400;
        double downtimePercentage= ((double)downtimeInSecond/(double)totalSecondsInADay)*100;
        return 100.00-downtimePercentage;
    }

}
