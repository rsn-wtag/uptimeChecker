package com.example.uptimeChecker.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.persistence.criteria.CriteriaBuilder;
import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
public class UptimeStatusDTO {
    private boolean isDown;
    private String url;
    private OffsetDateTime time;

    private Integer webId;

}
