package com.example.uptimeChecker.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.persistence.criteria.CriteriaBuilder;
import java.time.OffsetDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
public class UptimeStatusDTO {
    private boolean isDown;
    private String url;
    private Date time;

    private Integer webId;

}
