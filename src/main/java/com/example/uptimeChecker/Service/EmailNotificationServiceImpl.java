package com.example.uptimeChecker.Service;

import com.example.uptimeChecker.DTO.EmailDetailsDTO;
import com.example.uptimeChecker.DTO.WebsiteDetailsDTO;
import com.example.uptimeChecker.Entities.User;
import com.example.uptimeChecker.Repositories.WebsiteDetailsRepository;
import com.example.uptimeChecker.Repositories.WebsiteUserMetaDataRepository;
import com.example.uptimeChecker.constants.RabbitmqConstants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class EmailNotificationServiceImpl implements EmailNotificationService {
    @Autowired
    private WebsiteDetailsRepository websiteDetailsRepository;
    @Autowired
    private EmailService emailService;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @RabbitListener(queues = RabbitmqConstants.QUEUE_NAME)
    public void sendDowntimeNotificationEmailFromQueue(String message){
        ObjectMapper mapper = new ObjectMapper();
        try {
           EmailDetailsDTO emailDetailsDTO= mapper.readValue(message, EmailDetailsDTO.class);
           emailService.sendSimpleMail(emailDetailsDTO);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public String sendMessage(EmailDetailsDTO emailDetailsDTO) {
        ObjectMapper mapper= new ObjectMapper();
        String emailDetailsDTOString;
        try {
            emailDetailsDTOString = mapper.writeValueAsString(emailDetailsDTO);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        amqpTemplate.convertAndSend(
                RabbitmqConstants.EXCHANGE_NAME    , RabbitmqConstants.ROUTING_KEY, emailDetailsDTOString);
        return "Message Sent";
    }

    @Override
    public String sendMessageToUsers(WebsiteDetailsDTO websiteDetailsDTO)  {
        Set<User> users= new HashSet<>();
        users.add(new User("risha","", true, "rishanaznin@gmail.com",""));
        //users =websiteDetailsRepository.findUserByWebId(websiteDetailsDTO.getWebId());
        EmailDetailsDTO emailDetailsDTO;
        if(users!=null){
            for(User user: users){
                emailDetailsDTO= new EmailDetailsDTO();
                emailDetailsDTO.setRecipient(user.getEmail());
                emailDetailsDTO.setSubject("Downtime Alert");
                emailDetailsDTO.setMsgBody("Your website"+websiteDetailsDTO.getUrl()+" is Down...");
                sendMessage(emailDetailsDTO);


            }

        }
        return "Sent";
    }

   /* private String createEmailBody(User user, WebsiteDetailsDTO websiteDetailsDTO){

    }*/

}
