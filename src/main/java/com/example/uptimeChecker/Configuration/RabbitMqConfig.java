package com.example.uptimeChecker.Configuration;

import com.example.uptimeChecker.Constants.RabbitmqConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {


    @Bean
    public Queue createEmailQueue(){
        return new Queue(RabbitmqConstants.EMAIL_QUEUE_NAME,true);

    }

    @Bean

    public Queue createSlackQueue(){
        return new Queue(RabbitmqConstants.SLACK_QUEUE_NAME,true);

    }

    @Bean
    public Exchange createExchange(){
        return new TopicExchange(RabbitmqConstants.EXCHANGE_NAME, true, false);

    }
    @Bean
    public Binding createEmailQueueBinding(){
        return new Binding(RabbitmqConstants.EMAIL_QUEUE_NAME, Binding.DestinationType.QUEUE, RabbitmqConstants.EXCHANGE_NAME, RabbitmqConstants.EMAIL_ROUTING_KEY,null);
    }

    @Bean
    public Binding createSlackQueueBinding(){
        return new Binding(RabbitmqConstants.SLACK_QUEUE_NAME, Binding.DestinationType.QUEUE, RabbitmqConstants.EXCHANGE_NAME, RabbitmqConstants.SLACK_ROUTING_KEY,null);
    }

}
