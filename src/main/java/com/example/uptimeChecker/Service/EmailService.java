package com.example.uptimeChecker.Service;


import com.example.uptimeChecker.DTO.EmailDetailsDTO;

public interface EmailService {

    String sendSimpleMail(EmailDetailsDTO details);

}
