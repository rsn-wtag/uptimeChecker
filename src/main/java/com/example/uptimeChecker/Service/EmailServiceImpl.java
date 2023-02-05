package com.example.uptimeChecker.Service;


import com.example.uptimeChecker.DTO.EmailDetailsDTO;
import com.example.uptimeChecker.DTO.UserDTO;
import com.example.uptimeChecker.DTO.WebsiteDetailsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

// Annotation
@Service
// Class
// Implementing EmailService interface
public class EmailServiceImpl implements EmailService {

    @Autowired private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String sender;


    public void sendMail(EmailDetailsDTO details) throws MessagingException {
        MimeMessage mimeMessage=javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper= new MimeMessageHelper(mimeMessage, true);

        mimeMessageHelper.setText("",details.getMsgBody());
        mimeMessageHelper.setFrom(sender);
        mimeMessageHelper.setTo(details.getRecipient());
        mimeMessageHelper.setSubject(details.getSubject());

        javaMailSender.send(mimeMessage);

    }

    @Override
    public String createEmailBody(UserDTO user, WebsiteDetailsDTO websiteDetailsDTO){
        return  "<p>Hello "+user.getUserName()+", </p>"+
                "<p>Your registered website at Uptime Checker is Down.</p>"+
                "<p> <a href='"+websiteDetailsDTO.getUrl()+"'> "+websiteDetailsDTO.getUrl()+"</a> </p>"+
                " <p>Sincerely,<br>The Uptime Checker Team</p>";
    }

}
