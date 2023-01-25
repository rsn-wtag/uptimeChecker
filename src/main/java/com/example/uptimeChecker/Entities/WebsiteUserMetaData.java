package com.example.uptimeChecker.Entities;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "website_user_metadata")
@Data
public class WebsiteUserMetaData implements Serializable {

    @EmbeddedId
    private WebsiteUserMetaData_PK websiteUserMetaDataPk;
    @Column(name = "wb_name")
    private String websiteName;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wb_id", referencedColumnName = "web_id" ,insertable = false ,updatable = false)
    private WebsiteDetails websiteDetails;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id" ,insertable = false ,updatable = false)
    private User user;


}
