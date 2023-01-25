package com.example.uptimeChecker.DTO;

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
}
