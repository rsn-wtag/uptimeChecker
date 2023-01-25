package com.example.uptimeChecker.Service;

import com.example.uptimeChecker.DTO.DownTimeSummaryDTO;
import com.example.uptimeChecker.DTO.WebsiteDetailsDTO;

import java.util.Set;
import java.util.concurrent.TimeUnit;

public interface DowntimeService {

    public void saveDowntimeInfo(WebsiteDetailsDTO websiteDetailsDTO);

    public void updateDowntimeInfo(WebsiteDetailsDTO websiteDetailsDTO);

    void updateEndOfDowntime(TimeUnit timeUnit, Integer period);

    Set<DownTimeSummaryDTO> getDayWiseDownTimeHistory(Integer webId);
}
