package com.example.uptimeChecker.Service;

import com.example.uptimeChecker.DTO.SlackMessageDetailDTO;
import com.example.uptimeChecker.DTO.WebsiteDetailsDTO;

public interface SlackMessageService {
    void sendSlackMessage(SlackMessageDetailDTO slackMessageDetailDTO);

    String createSlackMessage(WebsiteDetailsDTO websiteDetailsDTO);
}
