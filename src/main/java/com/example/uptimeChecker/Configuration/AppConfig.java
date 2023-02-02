package com.example.uptimeChecker.Configuration;

import com.example.uptimeChecker.DTO.WebsiteChangeInfoDTO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

@Configuration
public class AppConfig {

    @Bean
    public BlockingQueue<WebsiteChangeInfoDTO> websiteChangeTrackerQueue(){
        return new LinkedBlockingDeque<>();
    }

}
