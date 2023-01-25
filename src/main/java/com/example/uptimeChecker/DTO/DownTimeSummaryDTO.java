package com.example.uptimeChecker.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DownTimeSummaryDTO {
    private Date date;
    private Integer webId;
    private long totalDownTime;
    private Double uptimePercentage;
    private long totalUpTime;


}
