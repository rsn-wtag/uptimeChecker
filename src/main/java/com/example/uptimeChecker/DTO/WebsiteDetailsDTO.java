package com.example.uptimeChecker.DTO;

import com.googlecode.jmapper.annotations.JGlobalMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    public WebsiteDetailsDTO(Integer webId, String url) {
        this.webId = webId;
        this.url = url;
        this.failCount=0;
        this.totalConsecutiveFailCount=0;
        this.future=null;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
