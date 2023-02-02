package com.example.uptimeChecker.Service;

import com.example.uptimeChecker.Constants.RabbitmqConstants;
import com.example.uptimeChecker.DTO.EmailDetailsDTO;
import com.example.uptimeChecker.DTO.SlackMessageDetailDTO;
import com.example.uptimeChecker.DTO.UserDTO;
import com.example.uptimeChecker.DTO.WebsiteDetailsDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import java.util.Set;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {
    @Autowired
    private WebsiteUserService websiteUserService;
    @Autowired
    private EmailService emailService;

    @Autowired
    private SlackMessageService slackMessageService;
    @Autowired
    private AmqpTemplate amqpTemplate;

    @Value("${slack.channel}")
    private String slackChannelName;
    @Value("${slack.bot.name}")
    private String  userName;

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);
    @RabbitListener(queues = RabbitmqConstants.EMAIL_QUEUE_NAME)
    public void sendDowntimeNotificationEmailFromQueue(String message){
        ObjectMapper mapper = new ObjectMapper();
        try {
           EmailDetailsDTO emailDetailsDTO= mapper.readValue(message, EmailDetailsDTO.class);
           emailService.sendMail(emailDetailsDTO);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            logger.error("Some error occurred at sendDowntimeNotificationEmailFromQueue "+e.getMessage());
        } catch (MessagingException e) {
            e.printStackTrace();
            logger.error("Some error occurred at sendDowntimeNotificationEmailFromQueue "+e.getMessage());
        }

    }

    @RabbitListener(queues = RabbitmqConstants.SLACK_QUEUE_NAME)
    public void sendDowntimeSlackNotificationFromQueue(String message){
        ObjectMapper mapper = new ObjectMapper();
        try {
            SlackMessageDetailDTO slackMessageDetailDTO= mapper.readValue(message, SlackMessageDetailDTO.class);
            slackMessageService.sendSlackMessage(slackMessageDetailDTO);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            logger.error("Some error occurred at sendDowntimeSlackNotificationFromQueue "+e.getMessage());

        }

    }

    @Override
    public void sendMessageToUsers(WebsiteDetailsDTO websiteDetailsDTO)  {
        Set<UserDTO> users;
       // users.add(new User("risha","", true, "rishanaznin@gmail.com",""));
        users =websiteUserService.getUsersByWebsite(websiteDetailsDTO.getWebId());
        EmailDetailsDTO emailDetailsDTO;
        SlackMessageDetailDTO slackMessageDetailDTO;
        if(users.size()>0){
            for(UserDTO user: users){
                if(user.getEmail()!=null && !user.getEmail().isEmpty()) {
                    emailDetailsDTO = new EmailDetailsDTO();
                    emailDetailsDTO.setRecipient(user.getEmail());
                    emailDetailsDTO.setSubject("Downtime Alert");
                    emailDetailsDTO.setMsgBody(emailService.createEmailBody(user, websiteDetailsDTO));
                    try {
                        sendEmailToRabbitMq(emailDetailsDTO);
                    } catch (Exception e) {
                        e.printStackTrace();
                        logger.error("Unable to send message to email queue");
                    }
                }
                if(user.getSlackId()!=null && !user.getSlackId().isEmpty()){
                    slackMessageDetailDTO= new SlackMessageDetailDTO();
                    slackMessageDetailDTO.setChannelName(slackChannelName);
                    slackMessageDetailDTO.setUsername(userName);
                    slackMessageDetailDTO.setMessage(slackMessageService.createSlackMessage(websiteDetailsDTO));
                    slackMessageDetailDTO.setWebhookUrl(user.getSlackId());

                    try {
                        sendSlackNotificationToRabbitMQ(slackMessageDetailDTO);
                    } catch (Exception e) {
                        e.printStackTrace();
                        logger.error("Unable to send message to slack queue");

                    }
                }
            }

        }

    }




    private void sendSlackNotificationToRabbitMQ(SlackMessageDetailDTO slackMessageDetailDTO) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String slackMessageDetailsDTOString;
        try {
            slackMessageDetailsDTOString = mapper.writeValueAsString(slackMessageDetailDTO);
            amqpTemplate.convertAndSend(RabbitmqConstants.EXCHANGE_NAME, RabbitmqConstants.SLACK_ROUTING_KEY, slackMessageDetailsDTOString);
        } catch (JsonProcessingException e) {
            throw e;
        }
    }

    private void sendEmailToRabbitMq(EmailDetailsDTO emailDetailsDTO) throws JsonProcessingException {
        ObjectMapper mapper= new ObjectMapper();
        String emailDetailsDTOString;
        try {
            emailDetailsDTOString = mapper.writeValueAsString(emailDetailsDTO);
            amqpTemplate.convertAndSend(
                    RabbitmqConstants.EXCHANGE_NAME    , RabbitmqConstants.EMAIL_ROUTING_KEY, emailDetailsDTOString);
        } catch (JsonProcessingException e) {
            throw e;
        }

    }

}
