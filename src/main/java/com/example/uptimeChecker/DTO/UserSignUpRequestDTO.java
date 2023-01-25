package com.example.uptimeChecker.DTO;

import lombok.Data;

@Data
public class UserSignUpRequestDTO {
    private String username;
    private char[] password;
    private String email;
    private String slackId;

}
