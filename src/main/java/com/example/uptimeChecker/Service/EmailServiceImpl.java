package com.example.uptimeChecker.Service;




import java.io.File;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;


import com.example.uptimeChecker.DTO.EmailDetailsDTO;
import com.example.uptimeChecker.Exceptions.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

// Annotation
@Service
// Class
// Implementing EmailService interface
public class EmailServiceImpl implements EmailService {

    @Autowired private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String sender;


    public String sendMail(EmailDetailsDTO details)
    {
        try {

            MimeMessage mimeMessage=javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper= new MimeMessageHelper(mimeMessage, true);

            mimeMessageHelper.setText("",details.getMsgBody());
            mimeMessageHelper.setFrom(sender);
            mimeMessageHelper.setTo(details.getRecipient());
            mimeMessageHelper.setSubject(details.getSubject());


            javaMailSender.send(mimeMessage);
            return "Mail Sent Successfully...";
        }


        catch (Exception e) {
            throw new CustomException(e.getMessage(), "error.email.sending");
        }
    }



}
