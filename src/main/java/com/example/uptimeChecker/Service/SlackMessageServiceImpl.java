package com.example.uptimeChecker.Service;

import com.example.uptimeChecker.DTO.SlackMessageDetailDTO;
import com.example.uptimeChecker.DTO.WebsiteDetailsDTO;
import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.webhook.Payload;
import com.github.seratch.jslack.api.webhook.WebhookResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SlackMessageServiceImpl implements SlackMessageService {


    @Override
    public void sendSlackMessage(SlackMessageDetailDTO slackMessageDetailDTO) {
        Payload payload= Payload.builder().
                channel(slackMessageDetailDTO.getChannelName())
                .username(slackMessageDetailDTO.getUsername())
                .text(slackMessageDetailDTO.getMessage())
                .build();
        try {
            WebhookResponse webhookResponse= Slack.getInstance().send(slackMessageDetailDTO.getWebhookUrl(), payload);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String createSlackMessage(WebsiteDetailsDTO websiteDetailsDTO){
        return "Your website is Down. "+websiteDetailsDTO.getUrl();
    }
}
