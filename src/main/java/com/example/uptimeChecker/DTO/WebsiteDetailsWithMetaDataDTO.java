package com.example.uptimeChecker.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WebsiteDetailsWithMetaDataDTO {

    @NonNull
    private Integer userId;
    @NonNull
    private Integer webId;

    @NonNull
    private String url;

    private String websiteName;

    public WebsiteDetailsWithMetaDataDTO(Integer webId, String url, String websiteName) {
        this.webId = webId;
        this.url = url;
        this.websiteName = websiteName;
    }
}
