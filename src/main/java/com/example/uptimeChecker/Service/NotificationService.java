package com.example.uptimeChecker.Service;

import com.example.uptimeChecker.DTO.WebsiteDetailsDTO;

public interface NotificationService {

    void sendMessageToUsers(WebsiteDetailsDTO websiteDetailsDTO);
}
