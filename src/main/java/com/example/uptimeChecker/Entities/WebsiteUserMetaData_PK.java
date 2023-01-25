package com.example.uptimeChecker.Entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Id;
import java.io.Serializable;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class WebsiteUserMetaData_PK implements Serializable {

    @Column(name = "user_id")
    private Integer userId;
    @Column(name = "wb_id")
    private Integer wbId;
}
