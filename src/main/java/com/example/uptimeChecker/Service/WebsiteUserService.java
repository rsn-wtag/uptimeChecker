package com.example.uptimeChecker.Service;

import com.example.uptimeChecker.DTO.UserDTO;
import com.example.uptimeChecker.DTO.WebsiteDetailsWithMetaDataDTO;

import java.util.Set;

public interface WebsiteUserService {


     Set<WebsiteDetailsWithMetaDataDTO> getWebsiteDetailsByUser(Integer userId);
    String saveWebsite(WebsiteDetailsWithMetaDataDTO websiteDetailsWithMetaData, boolean isRegister);
     void removeWebsite(Integer userId, Integer webId);
     WebsiteDetailsWithMetaDataDTO getWebsite(Integer userId, Integer webId);

     void updateUserWebsiteInfo(WebsiteDetailsWithMetaDataDTO websiteDetailsWithMetaDataDTO);


}
