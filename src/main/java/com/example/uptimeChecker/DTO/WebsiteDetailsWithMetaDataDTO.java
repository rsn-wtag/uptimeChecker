package com.example.uptimeChecker.DTO;

import com.example.uptimeChecker.DTO.website.WebsiteDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class WebsiteDetailsWithMetaDataDTO {
    private Integer userId;
    private Integer webId;
    private String url;
    private String websiteName;

    public WebsiteDetailsWithMetaDataDTO(Integer webId, String url, String websiteName) {
        this.webId = webId;
        this.url = url;
        this.websiteName = websiteName;
    }
    public WebsiteDetailsWithMetaDataDTO(WebsiteDTO websiteDTO, Integer userId){
        this.userId=userId;
        this.url = websiteDTO.getUrl();
        this.websiteName = websiteDTO.getWebsiteName();
    }
    public WebsiteDetailsWithMetaDataDTO(WebsiteDTO websiteDTO, Integer webId, Integer userId){
        this.webId=webId;
        this.userId=userId;
        this.url = websiteDTO.getUrl();
        this.websiteName = websiteDTO.getWebsiteName();
    }
}
