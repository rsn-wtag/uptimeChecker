package com.example.uptimeChecker.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SlackMessageDetailDTO {

    private String username;
    private String channelName;
    private String emojiIcon;
    private String message;

    private String webhookUrl;

}
