package com.example.uptimeChecker.Configuration;

import com.example.uptimeChecker.constants.RabbitmqConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {


    @Bean
    public Queue createQueue(){
        return new Queue(RabbitmqConstants.QUEUE_NAME,true);

    }

    @Bean
    public Exchange createExchange(){
        return new TopicExchange(RabbitmqConstants.EXCHANGE_NAME, true, false);

    }
    @Bean
    public Binding createBinding(){
        return new Binding(RabbitmqConstants.QUEUE_NAME, Binding.DestinationType.QUEUE, RabbitmqConstants.EXCHANGE_NAME, RabbitmqConstants.ROUTING_KEY,null);
    }

}
