package com.example.uptimeChecker.Service;

import com.example.uptimeChecker.Component.CsvFIleGenerator;
import com.example.uptimeChecker.DTO.DownTimeDTO;
import com.example.uptimeChecker.DTO.DownTimeSummaryDTO;
import com.example.uptimeChecker.DTO.WebsiteDetailsDTO;
import com.example.uptimeChecker.Entities.Downtime;
import com.example.uptimeChecker.Entities.DowntimeSummary;
import com.example.uptimeChecker.Repositories.DowntimeRepository;
import com.example.uptimeChecker.Repositories.DowntimeSummaryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.OffsetTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class DowntimeServiceImpl implements DowntimeService {
    private static final Logger logger = LoggerFactory.getLogger(DowntimeServiceImpl.class);

    @Autowired
    DowntimeRepository downtimeRepository;
    @Autowired
    DowntimeSummaryRepository downtimeSummaryRepository;
    @Autowired
    CsvFIleGenerator csvFIleGenerator;

    @Value("${max.fail.count}")
    Integer maxFailCount;
    @Override
    @Transactional
    public Downtime saveDowntimeInfo(WebsiteDetailsDTO websiteDetailsDTO) {
        Downtime downtime;
        if(websiteDetailsDTO.getDowntimeId()!=null) {
            Optional<Downtime> downtimeOptional= downtimeRepository.findById(websiteDetailsDTO.getDowntimeId());
            if(downtimeOptional.isPresent() && downtimeOptional.get().getEndTime()==null){
                downtime=  downtimeOptional.get();
                downtime.setTotalFailCount(websiteDetailsDTO.getTotalConsecutiveFailCount());
            }else {
                //downtime info is deleted/updated by EOD scheduler and also a new row must have been inserted
                downtime = downtimeRepository.findFirstByWebIdOrderByDownTimeIdDesc(websiteDetailsDTO.getWebId());
                downtime.setTotalFailCount(websiteDetailsDTO.getTotalConsecutiveFailCount());
            }
        }else{
            downtime = downtimeRepository.findFirstByWebIdOrderByDownTimeIdDesc(websiteDetailsDTO.getWebId());
            if(downtime==null || downtime.getEndTime()!=null){
                //no downtime occurred yet
                downtime = new Downtime(websiteDetailsDTO.getWebId(), OffsetTime.now(), new Date(), websiteDetailsDTO.getTotalConsecutiveFailCount());
            }else{
                //just update previously inserted downtime
                downtime.setTotalFailCount(websiteDetailsDTO.getTotalConsecutiveFailCount());
            }
        }
            return  downtimeRepository.save(downtime);



    }

    @Override
    @Transactional
    public Downtime updateDowntimeInfo(WebsiteDetailsDTO websiteDetailsDTO){

        Downtime downtime;
        if(websiteDetailsDTO.getDowntimeId()!=null) {
            Optional<Downtime> downtimeOptional= downtimeRepository.findById(websiteDetailsDTO.getDowntimeId());
            if(downtimeOptional.isPresent() && downtimeOptional.get().getEndTime()==null){
                 downtime= downtimeOptional.get();
            }else{
                //EOD scheduler may have deleted/updated the row and also must have inserted a new row
                downtime = downtimeRepository.findFirstByWebIdOrderByDownTimeIdDesc(websiteDetailsDTO.getWebId());
            }
            downtime.setEndTime(OffsetTime.now());
            downtime.setTotalFailCount(websiteDetailsDTO.getTotalConsecutiveFailCount());
            return downtimeRepository.save(downtime);
        }else{
            downtime = downtimeRepository.findFirstByWebIdOrderByDownTimeIdDesc(websiteDetailsDTO.getWebId());
            if(downtime!=null && downtime.getEndTime()==null){
                //just update previously inserted downtime
                downtime.setTotalFailCount(websiteDetailsDTO.getTotalConsecutiveFailCount());
                return downtimeRepository.save(downtime);
            }else{
                return new Downtime();
            }
        }


    }

    @Override
    public void updateEndOfDowntime(TimeUnit timeUnit, Integer period) {
        List<Downtime> downtimeList=  downtimeRepository.findByEndTime(null);
        for (Downtime downtime:downtimeList){
            downtime.setEndTime(calculateApproxEndOfDownTime(downtime.getStartTime(), downtime.getTotalFailCount(), timeUnit, period));
            downtimeRepository.save(downtime);
        }
    }
    @Override
    public List<DownTimeSummaryDTO> getDayWiseDownTimeHistory(Integer webId){
        List<DowntimeSummary> downtimeSummarySet= downtimeSummaryRepository.findDowntimeSummariesByDowntimeSummaryPkWebIdOrderByDowntimeSummaryPkDateDesc(webId);
        List<DownTimeSummaryDTO> downTimeSummaryDTOS= new ArrayList<>();

       downtimeSummarySet.forEach(downtimeSummary -> {
          downTimeSummaryDTOS.add( new DownTimeSummaryDTO(downtimeSummary.getDowntimeSummaryPk().getDate(),
                  downtimeSummary.getDowntimeSummaryPk().getWebId(),
                  downtimeSummary.getTotalDownTime(),
                  downtimeSummary.getUptimePercentage(),
                  86400-downtimeSummary.getTotalDownTime()
          ));
      });
       return downTimeSummaryDTOS;
    }

    @Override
    public List<DownTimeDTO> getTodayDownTimeHistory(Integer webId) {
        List<DownTimeDTO> downTimeDTOS= new ArrayList<>();

       downtimeRepository.findByDateAndWebId(new Date(), webId).forEach(downtime -> {
           Date startTime=  Date.from(downtime.getStartTime().atDate(LocalDate.now()).toInstant());

           Date endTime=null;
           if(downtime.getEndTime() !=null)
               endTime=Date.from(downtime.getEndTime().atDate(LocalDate.now()).toInstant());
           downTimeDTOS.add(new DownTimeDTO(downtime.getDownTimeId(), downtime.getWebId(),startTime, endTime, downtime.getDate(), downtime.getTotalFailCount()));
       });
       return  downTimeDTOS;
    }

    //calculates approx downtime from failCount with the same logic which is used to calculate back off time
    private OffsetTime calculateApproxEndOfDownTime(OffsetTime startTime, Integer failCount, TimeUnit timeUnit, Integer period){
        failCount=failCount-maxFailCount;
        Integer failedPeriod=0;
        long approxDownTimeInMillis;
        for(int i=0, j=1; i<failCount; i++){
            failedPeriod=((int) (period*Math.pow(2,j))) +failedPeriod;
            if(j==maxFailCount) j=1;
            else j++;
        }
        approxDownTimeInMillis= timeUnit.toMillis(failedPeriod);
        return  startTime.plus(approxDownTimeInMillis, ChronoUnit.MILLIS);
    }

    @Override
    public List<BigInteger> getDowntimeIdsToBeUpdatedAtEOD(Date currentDate){
        List<BigInteger> downTimeDTOSet = new ArrayList<>();
        List<Downtime> downtimeSet=  downtimeRepository.findByDateAndEndTimeGreaterThanStartTime(currentDate);
        downtimeSet.forEach(downtime -> {
            downTimeDTOSet.add(downtime.getDownTimeId());
        });
        return downTimeDTOSet;
    }

    @Override
    @Transactional
    public void updateDownTimeAtEOD(BigInteger downtimeId, OffsetTime todayEndTime, OffsetTime nextDayStartTime, Date nextDate){
        Optional<Downtime> downtimeOptional= downtimeRepository.findById(downtimeId);
        Downtime downtime=downtimeOptional.get();
        //re checking if end of  downtime is updated or not
        if(downtime.getEndTime()==null){
            //if not then just set endtime to null for new row
            downtime.setEndTime(todayEndTime);
            downtimeRepository.save(downtime);
            Downtime newDowntime= new Downtime(downtime.getWebId(), nextDayStartTime,nextDate,maxFailCount);
            downtimeRepository.save(newDowntime);
        }else{
            //if yes then insert new row with previous end time
            OffsetTime endTime= downtime.getEndTime();
            downtime.setEndTime(todayEndTime);
            downtimeRepository.save(downtime);
            Downtime newDowntime= new Downtime(downtime.getWebId(), nextDayStartTime,endTime,nextDate,maxFailCount);
            downtimeRepository.save(newDowntime);
        }
    }

    @Override
    @Transactional
    public void ExportToFileAndDeletePreviousDayDateForEOD(Integer webId, Date date){
        downtimeRepository.deleteByWebIdAndDate(webId, date);
    }
    @Override
    public String exportToCSVFile(Date date)  {
        List<Downtime> downtimeList= downtimeRepository.findByDate(date);
        try {
            return csvFIleGenerator.writeDowntimeInfoToCSV(downtimeList,date);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Some error occurred at exportToCSVFile "+e.getMessage());
            return null;
        }
    }
}
