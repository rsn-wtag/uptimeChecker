package com.example.uptimeChecker.Component;

import com.example.uptimeChecker.DTO.WebsiteChangeInfoDTO;
import com.example.uptimeChecker.Enums.WebsiteChangeType;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.BlockingQueue;

@Component
public class WebsiteChangeListenerComponent implements Runnable {

    private final Thread thread;

    private final BlockingQueue<WebsiteChangeInfoDTO> websiteChangeTrackerQueue;

    private final CheckUptime checkUptime;

    public WebsiteChangeListenerComponent(BlockingQueue<WebsiteChangeInfoDTO> websiteChangeTrackerQueue,
                                          CheckUptime checkUptime) {
        thread = new Thread(this);
        this.websiteChangeTrackerQueue = websiteChangeTrackerQueue;
        this.checkUptime = checkUptime;
    }

    @Override
    public void run() {
        while(!Thread.interrupted()) {
            try {
                WebsiteChangeInfoDTO websiteChangeInfoDTO = websiteChangeTrackerQueue.take();
                if (websiteChangeInfoDTO.getWebsiteChangeType() == WebsiteChangeType.ADD) {
                    checkUptime.addForExecution(websiteChangeInfoDTO.getWebsiteDetailsDTO());
                } else {
                    checkUptime.cancelTask(websiteChangeInfoDTO.getWebsiteDetailsDTO().getWebId());
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @PostConstruct
    public void init() {
        thread.start();
    }

}
