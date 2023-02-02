package com.example.uptimeChecker.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;
import java.util.concurrent.ScheduledFuture;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WebsiteDetailsDTO implements Cloneable{

    private Integer webId;

    private String url;
    private ScheduledFuture<?> future;

    private int failCount;//calculates backoff delay with this

    private int totalConsecutiveFailCount;//determines website is down or not with this

    private BigInteger downtimeId;

    public WebsiteDetailsDTO(Integer webId, String url) {
        this.webId = webId;
        this.url = url;
        this.failCount=0;
        this.totalConsecutiveFailCount=0;
        this.future=null;
        this.downtimeId=null;
    }

    public WebsiteDetailsDTO(Integer webId, String url, ScheduledFuture<?> future) {
        this.webId = webId;
        this.url = url;
        this.failCount=0;
        this.totalConsecutiveFailCount=0;
        this.future=future;
        this.downtimeId=null;
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
