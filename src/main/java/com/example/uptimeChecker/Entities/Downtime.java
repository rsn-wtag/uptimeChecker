package com.example.uptimeChecker.Entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigInteger;
import java.time.OffsetTime;
import java.util.Date;

@Entity
@Table(name = "downtime_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Downtime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dt_id")
    private BigInteger downTimeId;


    @Column(name = "dt_web_id")
    private Integer webId;

    @Column(name = "dt_start_time")
    private OffsetTime startTime;

    @Column(name = "dt_end_time")
    private OffsetTime endTime;

    @Column(name = "dt_date")
    private Date date;

    @Column(name = "dt_fail_count")
    private Integer totalFailCount;
    public Downtime(Integer webId, OffsetTime startTime, Date date, Integer totalFailCount) {
        this.webId = webId;
        this.startTime = startTime;
        this.date=date;
        this.totalFailCount=totalFailCount;
    }

    public Downtime(Integer webId, OffsetTime startTime, OffsetTime endTime, Date date, Integer totalFailCount) {
        this.webId = webId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.date=date;
        this.totalFailCount=totalFailCount;
    }
}
