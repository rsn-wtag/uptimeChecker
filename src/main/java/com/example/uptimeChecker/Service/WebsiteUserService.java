package com.example.uptimeChecker.Service;

import com.example.uptimeChecker.DTO.WebsiteDetailsDTO;
import com.example.uptimeChecker.DTO.WebsiteDetailsWithMetaDataDTO;

import java.util.List;
import java.util.Set;

public interface WebsiteUserService {


    public Set<WebsiteDetailsWithMetaDataDTO> getWebsiteDetailsByUser(Integer userId);
    String saveWebsite(WebsiteDetailsWithMetaDataDTO websiteDetailsWithMetaData, boolean isRegister);
    public void removeWebsite(Integer userId, Integer webId);
    public WebsiteDetailsWithMetaDataDTO getWebsite(Integer userId, Integer webId);

    public void updateUserWebsiteInfo(WebsiteDetailsWithMetaDataDTO websiteDetailsWithMetaDataDTO);

}
