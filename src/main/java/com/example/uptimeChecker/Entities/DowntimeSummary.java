package com.example.uptimeChecker.Entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "downtime_summary")
public class DowntimeSummary {

    @EmbeddedId
    private DowntimeSummary_PK downtimeSummaryPk;

    @Column(name = "total_downtime")
    private long totalDownTime;

    @Column(name = "uptime_percentage")
    private Double uptimePercentage;


}
