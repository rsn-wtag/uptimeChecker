package com.example.uptimeChecker.Entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "website_detail")
@Getter
@Setter
public class WebsiteDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "web_id")
    private Integer webId;

    @Column(name = "wb_url")
    private String url;

    @OneToMany(mappedBy = "websiteDetails",fetch = FetchType.LAZY)
    private Set<WebsiteUserMetaData> websiteUserMetaDataSet;

    @Override
    public String toString() {
        return "WebsiteDetails{" +
                "webId=" + webId +
                ", url='" + url + '\'' +
                '}';
    }
}
