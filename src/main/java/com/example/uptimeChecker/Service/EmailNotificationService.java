package com.example.uptimeChecker.Service;

import com.example.uptimeChecker.DTO.EmailDetailsDTO;
import com.example.uptimeChecker.DTO.WebsiteDetailsDTO;

public interface EmailNotificationService {
    String sendMessage(EmailDetailsDTO emailDetailsDTO);

    String sendMessageToUsers(WebsiteDetailsDTO websiteDetailsDTO);
}
