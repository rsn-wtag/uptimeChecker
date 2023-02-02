package com.example.uptimeChecker.Component;

import com.example.uptimeChecker.DTO.WebsiteDetailsDTO;
import com.example.uptimeChecker.Entities.*;
import com.example.uptimeChecker.Enums.ProcessDayStatus;
import com.example.uptimeChecker.Repositories.DowntimeRepository;
import com.example.uptimeChecker.Repositories.DowntimeSummaryRepository;
import com.example.uptimeChecker.Repositories.EndOfDayInfoRepository;
import com.example.uptimeChecker.Repositories.EodProcessingDayRepository;
import com.example.uptimeChecker.Service.DowntimeService;
import com.example.uptimeChecker.Service.WebsiteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.OffsetTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
@EnableScheduling
public class DowntimeSummaryManagementComponent {
    private static final Logger logger = LoggerFactory.getLogger(DowntimeSummaryManagementComponent.class);
    @Autowired
    DowntimeSummaryRepository downtimeSummaryRepository;

    @Autowired
    DowntimeRepository downtimeRepository;
    @Autowired
    WebsiteService websiteService;

    @Autowired
    DowntimeService downtimeService;

    @Autowired
    EodProcessingDayRepository eodProcessingDayRepository;

    @Autowired
    EndOfDayInfoRepository endOfDayInfoRepository;


    //this whole task should be executed at db level with procedure, that would be better I think
    @Scheduled(cron = "0 0 0 * * *")//this method will be executed every day at 0:0:0
    //@Scheduled(cron = "0 31 11 * * *")//test
    public void executeEndOfDayTask(){
        Calendar c= Calendar.getInstance();
        if( eodProcessingDayRepository.findById(1).isPresent()) {
              Date currentDate = eodProcessingDayRepository.findAll().get(0).getNextProcessingDate();
              c.setTime(currentDate);
              c.add(Calendar.DATE, 1);
              Date nextDate= c.getTime();

              OffsetTime todayEndTime=OffsetTime.now().withHour(23).withMinute(59).withSecond(59);
              OffsetTime nextDayStartTime=OffsetTime.now().withHour(0).withMinute(0).withSecond(0);

                //get all of them whose end time is not yet updated.
                //update their end time. and insert another row with tomorrow  and start time as 00:00:00
                List<BigInteger> downtimeIdSet= downtimeService.getDowntimeIdsToBeUpdatedAtEOD(currentDate);
                for(BigInteger downtimeId:downtimeIdSet){
                    downtimeService.updateDownTimeAtEOD(downtimeId, todayEndTime, nextDayStartTime, nextDate);
                }

                String fileName=downtimeService.exportToCSVFile(currentDate);
                boolean deleteData=true;
                if(fileName==null) {
                    deleteData=false;
                    logger.error("Error Occurred at CSV file generation for date "+ currentDate);
                }
                List<WebsiteDetailsDTO> websiteDetailsDTOList = websiteService.getWesiteDetailList();
                for (WebsiteDetailsDTO websiteDetailsDTO : websiteDetailsDTOList) {
                    try {
                        if(!downtimeSummaryRepository.existsByDowntimeSummaryPk(new DowntimeSummary_PK(currentDate,websiteDetailsDTO.getWebId())))
                        //for all website calculate and save summary in the summary table
                        calculateAndSaveDowntimeSummary(websiteDetailsDTO.getWebId(), currentDate, deleteData);
                    } catch (Exception e) {
                        logger.error("Error Occurred at summary Calculation/data deletion for WebID " + websiteDetailsDTO.getWebId() + ", date: " + currentDate);
                    }
                }
            updateEodInfo(currentDate, ProcessDayStatus.Success, fileName);
        }else{
            logger.error("Unable to process EOD. no processing date found ");

        }

    }
    @Transactional
    public boolean executeEndOfDayTaskForMissedScheduler(){
       // Downtime lastDowntimeInfo=downtimeRepository.findFirstByOrderByDateDesc();
       if( eodProcessingDayRepository.findById(1).isPresent()) {
           Date processingDate = eodProcessingDayRepository.findAll().get(0).getNextProcessingDate();

           SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

           if (processingDate != null && !sdf.format(processingDate).equals(sdf.format(new Date()))) {
               //if down time info has previous date data then scheduler has been missed
               List<WebsiteDetailsDTO> websiteDetailsList = websiteService.getWesiteDetailList();

               downtimeRepository.updateAllNullEndTime(OffsetTime.now().withHour(23).withMinute(59).withSecond(59));
               String fileName = downtimeService.exportToCSVFile(processingDate);
               boolean deleteData=true;
               if (fileName==null) {
                   deleteData=false;
                   logger.error("Error Occurred at CSV file generation for date " + processingDate);
               }
               for (WebsiteDetailsDTO websiteDetailsDTO : websiteDetailsList) {
                   if(!downtimeSummaryRepository.existsByDowntimeSummaryPk(new DowntimeSummary_PK(processingDate,websiteDetailsDTO.getWebId())))
                       calculateAndSaveDowntimeSummary(websiteDetailsDTO.getWebId(), processingDate, deleteData);
               }

               updateEodInfo(processingDate, ProcessDayStatus.Success, fileName);
               return true;
           }
       }
       return false;
    }
    @Transactional
    public void calculateAndSaveDowntimeSummary(Integer webId, Date date, boolean deleteData){

          List<Downtime> DowntimeList= downtimeRepository.findByDateAndWebId(date, webId);
          long downtimeInSecond =0;
              //calculating total downtime
              for(Downtime downtime:DowntimeList){
                  if(downtime.getEndTime()!=null){
                      downtimeInSecond=  Duration.between(downtime.getStartTime(), downtime.getEndTime()).getSeconds()+ downtimeInSecond;
                  }
              }
            DowntimeSummary downtimeSummary= new DowntimeSummary();
            downtimeSummary.setDowntimeSummaryPk(new DowntimeSummary_PK(date,webId));
            downtimeSummary.setTotalDownTime(downtimeInSecond);
            downtimeSummary.setUptimePercentage(calculateUptimePercentage(downtimeInSecond));
            downtimeSummaryRepository.save(downtimeSummary);
            if(deleteData)
                //delete all data for that day for that specific website if data is successfully backed ip in csv file.
                downtimeService.ExportToFileAndDeletePreviousDayDateForEOD(webId,date);

    }


    private double calculateUptimePercentage(long downtimeInSecond){
        long totalSecondsInADay = 86400;
        double downtimePercentage= ((double)downtimeInSecond/(double)totalSecondsInADay)*100;
        return 100.00-downtimePercentage;
    }

    private void updateEodInfo(Date processedDate, ProcessDayStatus status, String backupFileName){
        Optional<EodProcessingDay> eodProcessingDayOptional=eodProcessingDayRepository.findById(1);
        if(eodProcessingDayOptional.isPresent()){
            EodProcessingDay eodProcessingDay = eodProcessingDayOptional.get();
            eodProcessingDay.setNextProcessingDate(new Date());
            eodProcessingDayRepository.save(eodProcessingDay);

            EndOfDayInfo endOfDayInfo = new EndOfDayInfo(processedDate, status, backupFileName);
            endOfDayInfoRepository.save(endOfDayInfo);
        }

    }

}
