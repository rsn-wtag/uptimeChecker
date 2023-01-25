package com.example.uptimeChecker.Entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class DowntimeSummary_PK implements Serializable {

    @Column(name = "date")
    private Date  date;


    @Column(name = "web_id")
    private Integer webId;
}
