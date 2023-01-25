package com.example.uptimeChecker.Entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;
import java.math.BigInteger;
import java.time.OffsetDateTime;

@Entity
@Table(name = "downtime_info")
@Data
@NoArgsConstructor
public class Downtime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dt_id")
    private BigInteger downTimeId;


    @Column(name = "dt_web_id")
    private Integer webId;

    @Column(name = "dt_start_time")
    private OffsetDateTime startTime;

    @Column(name = "dt_end_time")
    private OffsetDateTime endTime;

    public Downtime(Integer webId, OffsetDateTime startTime) {
        this.webId = webId;
        this.startTime = startTime;
    }

    public Downtime(Integer webId, OffsetDateTime startTime, OffsetDateTime endTime) {
        this.webId = webId;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
