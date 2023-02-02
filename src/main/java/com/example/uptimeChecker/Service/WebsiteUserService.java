package com.example.uptimeChecker.Service;

import com.example.uptimeChecker.DTO.UserDTO;
import com.example.uptimeChecker.DTO.WebsiteDetailsWithMetaDataDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

public interface WebsiteUserService {


     Set<WebsiteDetailsWithMetaDataDTO> getWebsiteDetailsByUser(Integer userId);
    void saveWebsite(WebsiteDetailsWithMetaDataDTO websiteDetailsWithMetaData, boolean isRegister);
     void removeWebsite(Integer userId, Integer webId);
     WebsiteDetailsWithMetaDataDTO getWebsite(Integer userId, Integer webId);

     void updateUserWebsiteInfo(WebsiteDetailsWithMetaDataDTO websiteDetailsWithMetaDataDTO);


    @Transactional
    Set<UserDTO> getUsersByWebsite(Integer webId);
}
