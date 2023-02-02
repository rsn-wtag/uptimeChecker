package com.example.uptimeChecker.Service;

import com.example.uptimeChecker.DTO.DownTimeDTO;
import com.example.uptimeChecker.DTO.DownTimeSummaryDTO;
import com.example.uptimeChecker.DTO.WebsiteDetailsDTO;
import com.example.uptimeChecker.Entities.Downtime;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.OffsetTime;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public interface DowntimeService {

     Downtime saveDowntimeInfo(WebsiteDetailsDTO websiteDetailsDTO);

     Downtime updateDowntimeInfo(WebsiteDetailsDTO websiteDetailsDTO);

    void updateEndOfDowntime(TimeUnit timeUnit, Integer period);

    List<DownTimeSummaryDTO> getDayWiseDownTimeHistory(Integer webId);
    List<DownTimeDTO> getTodayDownTimeHistory(Integer webId);

    List<BigInteger> getDowntimeIdsToBeUpdatedAtEOD(Date currentDate);

    @Transactional
    void updateDownTimeAtEOD(BigInteger downtimeId, OffsetTime todayEndTime, OffsetTime nextDayStartTime, Date nextDate);

    @Transactional
    void ExportToFileAndDeletePreviousDayDateForEOD(Integer webId, Date date);

    String exportToCSVFile(Date date) ;
}
