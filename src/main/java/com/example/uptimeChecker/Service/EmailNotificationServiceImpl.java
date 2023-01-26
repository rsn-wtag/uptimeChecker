package com.example.uptimeChecker.Service;

import com.example.uptimeChecker.DTO.EmailDetailsDTO;
import com.example.uptimeChecker.DTO.UserDTO;
import com.example.uptimeChecker.DTO.WebsiteDetailsDTO;
import com.example.uptimeChecker.Entities.User;
import com.example.uptimeChecker.Entities.WebsiteDetails;
import com.example.uptimeChecker.Entities.WebsiteUserMetaData;
import com.example.uptimeChecker.Repositories.WebsiteDetailsRepository;
import com.example.uptimeChecker.constants.RabbitmqConstants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
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
           emailService.sendMail(emailDetailsDTO);
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
        Set<UserDTO> users= new HashSet<>();
       // users.add(new User("risha","", true, "rishanaznin@gmail.com",""));
        users =getUsersByWebsite(websiteDetailsDTO.getWebId());
        EmailDetailsDTO emailDetailsDTO;
        if(users.size()>0){
            for(UserDTO user: users){
                emailDetailsDTO= new EmailDetailsDTO();
                emailDetailsDTO.setRecipient(user.getEmail());
                emailDetailsDTO.setSubject("Downtime Alert");
                emailDetailsDTO.setMsgBody(createEmailBody(user,websiteDetailsDTO));
                sendMessage(emailDetailsDTO);
            }

        }
        return "Sent";
    }

    @Transactional
    public Set<UserDTO> getUsersByWebsite(Integer webId) {
        Optional<WebsiteDetails> websiteDetailsOptional= websiteDetailsRepository.findById(webId);
        Set<UserDTO> userDTOSet= new HashSet<>();
        if(websiteDetailsOptional.isPresent()){
            WebsiteDetails websiteDetails= websiteDetailsOptional.get();
            Set<WebsiteUserMetaData> websiteUserMetaDataSet= websiteDetails.getWebsiteUserMetaDataSet();
            for(WebsiteUserMetaData websiteUserMetaData:websiteUserMetaDataSet){
                User user=  websiteUserMetaData.getUser();
                userDTOSet.add(new UserDTO(user.getUserId(), user.getUserName(), "", true, user.getEmail(), user.getSlackId()));
            }
        }

        return userDTOSet;
    }
    private String createEmailBody(UserDTO user, WebsiteDetailsDTO websiteDetailsDTO){
        return  "<p>Hello "+user.getUserName()+"</p>"+
                "<p>Your registered website at Uptime Checker is Down.</p>"+
                "<p> <a href='"+websiteDetailsDTO.getUrl()+"'> "+websiteDetailsDTO.getUrl()+"</a> </p>"+
                " <p>Sincerely,<br>The Uptime Checker Team</p>";
    }

}
