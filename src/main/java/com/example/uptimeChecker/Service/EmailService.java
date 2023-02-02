package com.example.uptimeChecker.Service;


import com.example.uptimeChecker.DTO.EmailDetailsDTO;
import com.example.uptimeChecker.DTO.UserDTO;
import com.example.uptimeChecker.DTO.WebsiteDetailsDTO;

import javax.mail.MessagingException;

public interface EmailService {

    void sendMail(EmailDetailsDTO details) throws MessagingException;

    String createEmailBody(UserDTO user, WebsiteDetailsDTO websiteDetailsDTO);
}
