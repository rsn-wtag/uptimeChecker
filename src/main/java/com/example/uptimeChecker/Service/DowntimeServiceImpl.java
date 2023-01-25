package com.example.uptimeChecker.Service;

import com.example.uptimeChecker.DTO.DownTimeSummaryDTO;
import com.example.uptimeChecker.DTO.WebsiteDetailsDTO;
import com.example.uptimeChecker.Entities.Downtime;
import com.example.uptimeChecker.Entities.DowntimeSummary;
import com.example.uptimeChecker.Exceptions.CustomException;
import com.example.uptimeChecker.Repositories.DowntimeRepository;
import com.example.uptimeChecker.Repositories.DowntimeSummaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.OffsetTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class DowntimeServiceImpl implements DowntimeService {
    @Autowired
    DowntimeRepository downtimeRepository;
    @Autowired
    DowntimeSummaryRepository downtimeSummaryRepository;
    @Value("${max.fail.count}")
    Integer maxFailCount;
    @Override
    public void saveDowntimeInfo(WebsiteDetailsDTO websiteDetailsDTO) {
        try {
            if (websiteDetailsDTO.getTotalConsecutiveFailCount() == maxFailCount) {
                Downtime downtime = new Downtime(websiteDetailsDTO.getWebId(), OffsetTime.now(), new Date(), websiteDetailsDTO.getTotalConsecutiveFailCount());
                downtimeRepository.save(downtime);
            }

        } catch (Exception e) {
            throw new CustomException(e.getMessage(), "unableToSave.message");
        }

    }

    @Override
    public void updateDowntimeInfo(WebsiteDetailsDTO websiteDetailsDTO){
        try {
            Downtime downtime= downtimeRepository.findFirstByWebIdOrderByDownTimeIdDesc(websiteDetailsDTO.getWebId());
            if (downtime!=null && downtime.getEndTime()==null) {
                downtime.setEndTime(OffsetTime.now());
                downtimeRepository.save(downtime);
            }

        } catch (Exception e) {
            throw new CustomException(e.getMessage(), "unableToUpdate.message");
        }
    }

    @Override
    public void updateEndOfDowntime(TimeUnit timeUnit, Integer period) {
        Set<Downtime> downtimeSet=  downtimeRepository.findByEndTime(null);
        for (Downtime downtime:downtimeSet){
            downtime.setEndTime(calculateApproxEndOfDownTime(downtime.getStartTime(), downtime.getTotalFailCount(), timeUnit, period));
            downtimeRepository.save(downtime);
        }
    }
    @Override
    public Set<DownTimeSummaryDTO> getDayWiseDownTimeHistory(Integer webId){
       Set<DowntimeSummary> downtimeSummarySet= downtimeSummaryRepository.findDowntimeSummariesByDowntimeSummaryPkWebId(webId);
       Set<DownTimeSummaryDTO> downTimeSummaryDTOS= new HashSet<>();
       DownTimeSummaryDTO downTimeSummaryDTO= new DownTimeSummaryDTO();
       downtimeSummarySet.parallelStream().forEach(downtimeSummary -> {
          downTimeSummaryDTOS.add( new DownTimeSummaryDTO(downtimeSummary.getDowntimeSummaryPk().getDate(),
                  downtimeSummary.getDowntimeSummaryPk().getWebId(),
                  downtimeSummary.getTotalDownTime(),
                  downtimeSummary.getUptimePercentage(),
                  86400-downtimeSummary.getTotalDownTime()
          ));
      });
       return downTimeSummaryDTOS;
    }

    //calculates approx downtime from failCount with the same logic which is used to calculate back off time
    private OffsetTime calculateApproxEndOfDownTime(OffsetTime startTime, Integer failCount, TimeUnit timeUnit, Integer period){
        failCount=failCount-maxFailCount;
        Integer failedPeriod=0;
        long approxDownTimeInMillis=0;
        for(int i=0, j=1; i<failCount; i++){
            failedPeriod=((int) (period*Math.pow(2,j))) +failedPeriod;
            if(j==maxFailCount) j=0;
            else j++;
        }
        approxDownTimeInMillis= timeUnit.toMillis(failedPeriod);
        return  startTime.plus(approxDownTimeInMillis, ChronoUnit.MILLIS);
    }

}
