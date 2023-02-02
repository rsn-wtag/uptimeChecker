package com.example.uptimeChecker.Entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "eod_processing_day")
public class EodProcessingDay {
    @Id
    @Column(name = "ep_id")
    private Integer id;

    @Column(name = "ep_next_processing_date")
    private Date nextProcessingDate;
}
