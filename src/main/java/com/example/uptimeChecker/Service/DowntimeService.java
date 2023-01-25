package com.example.uptimeChecker.Service;

import com.example.uptimeChecker.DTO.WebsiteDetailsDTO;

public interface DowntimeService {

    public void saveDowntimeInfo(WebsiteDetailsDTO websiteDetailsDTO);

    public void updateDowntimeInfo(WebsiteDetailsDTO websiteDetailsDTO);
}
