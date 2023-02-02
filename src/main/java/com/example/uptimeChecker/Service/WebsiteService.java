package com.example.uptimeChecker.Service;

import com.example.uptimeChecker.DTO.WebsiteDetailsDTO;

import java.util.List;

public interface WebsiteService {
    List<WebsiteDetailsDTO> getWesiteDetailList();


    boolean isAnyWebsiteExists();
}
