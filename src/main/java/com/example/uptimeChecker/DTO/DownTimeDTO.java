package com.example.uptimeChecker.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import java.math.BigInteger;
import java.time.OffsetTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DownTimeDTO {
    private BigInteger downTimeId;

    private Integer webId;

    private Date startTime;

    private Date endTime;

    private Date date;

    private Integer totalFailCount;
}
