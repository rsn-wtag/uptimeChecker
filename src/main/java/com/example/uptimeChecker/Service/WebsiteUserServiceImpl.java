package com.example.uptimeChecker.Service;

import com.example.uptimeChecker.Component.CheckUptime;
import com.example.uptimeChecker.DTO.WebsiteDetailsDTO;
import com.example.uptimeChecker.DTO.WebsiteDetailsWithMetaDataDTO;
import com.example.uptimeChecker.Entities.User;
import com.example.uptimeChecker.Entities.WebsiteDetails;
import com.example.uptimeChecker.Entities.WebsiteUserMetaData;
import com.example.uptimeChecker.Entities.WebsiteUserMetaData_PK;
import com.example.uptimeChecker.Exceptions.CustomException;
import com.example.uptimeChecker.Exceptions.ResourceNotFoundException;
import com.example.uptimeChecker.Repositories.UserRepository;
import com.example.uptimeChecker.Repositories.WebsiteDetailsRepository;
import com.example.uptimeChecker.Repositories.WebsiteUserMetaDataRepository;
import com.googlecode.jmapper.JMapper;
import com.googlecode.jmapper.api.JMapperAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

import static com.googlecode.jmapper.api.JMapperAPI.attribute;
import static com.googlecode.jmapper.api.JMapperAPI.mappedClass;

@Service
public class WebsiteUserServiceImpl implements WebsiteUserService {
    @Autowired
    Environment env;
    @Autowired
    WebsiteDetailsRepository websiteDetailsRepository;

    @Autowired
    WebsiteUserMetaDataRepository websiteUserMetaDataRepository;
    @Autowired
    UserRepository userRepository;

    @Autowired
    private CheckUptime checkUptime;




    public Set<WebsiteDetailsWithMetaDataDTO> getWebsiteDetailsByUser(Integer userId) {
        Set<WebsiteDetailsWithMetaDataDTO> websiteSet= new HashSet<>();
        WebsiteDetailsWithMetaDataDTO websiteDetailsWithMetaDataDTO;

        try {
            Optional<User> optionalUser = userRepository.findById(userId);

            if (optionalUser.isPresent()) {
                for(WebsiteUserMetaData websiteUserMetaData : optionalUser.get().getMappedWebsiteDetails()){
                    websiteDetailsWithMetaDataDTO= new WebsiteDetailsWithMetaDataDTO(websiteUserMetaData.getWebsiteDetails().getWebId(),
                            websiteUserMetaData.getWebsiteDetails().getUrl(),websiteUserMetaData.getWebsiteName());
                    websiteSet.add(websiteDetailsWithMetaDataDTO);
                }
            } else {
                throw new ResourceNotFoundException("Unable to find user");
            }
        } catch (Exception e) {
            throw e;
        }
        return websiteSet;
    }

    @Override
    @Transactional
    public void removeWebsite(Integer userId, Integer webId) {
        if( websiteUserMetaDataRepository.findById(new WebsiteUserMetaData_PK(userId, webId)).isPresent()){
            WebsiteUserMetaData websiteUserMetaData=websiteUserMetaDataRepository.findById(new WebsiteUserMetaData_PK(userId, webId)).get();
            //delete the user website mapping with meta data
            websiteUserMetaDataRepository.delete(websiteUserMetaData);
            if(!websiteUserMetaDataRepository.existsByWebsiteUserMetaDataPkWbId(webId)){
                //if the website is not mapped with any other user then delete the website as well
                Optional<WebsiteDetails> websiteDetailsOptional = websiteDetailsRepository.findById(webId);
                if(websiteDetailsOptional.isPresent()){
                    WebsiteDetails websiteDetails= websiteDetailsOptional.get();
                    websiteDetailsRepository.delete(websiteDetails);
                    //also cancel the scheduled task
                    checkUptime.cancelTask(webId);
                }
            }

        }
    }

    @Override
    public WebsiteDetailsWithMetaDataDTO getWebsite(Integer userId, Integer webId) {
       if( websiteUserMetaDataRepository.findById(new WebsiteUserMetaData_PK(userId, webId)).isPresent()){
            WebsiteUserMetaData websiteUserMetaData= websiteUserMetaDataRepository.findById(new WebsiteUserMetaData_PK(userId, webId)).get();
            WebsiteDetailsWithMetaDataDTO websiteDetailsWithMetaDataDTO= new WebsiteDetailsWithMetaDataDTO();
            websiteDetailsWithMetaDataDTO.setWebId(webId);
            websiteDetailsWithMetaDataDTO.setUserId(userId);
            websiteDetailsWithMetaDataDTO.setUrl(websiteUserMetaData.getWebsiteDetails().getUrl());
            websiteDetailsWithMetaDataDTO.setWebsiteName(websiteUserMetaData.getWebsiteName());
            return websiteDetailsWithMetaDataDTO;
        }else{
           throw new CustomException(null, "error.website.not.found",HttpServletResponse.SC_BAD_REQUEST);
       }
    }
    @Override
    @Transactional
    public String saveWebsite(WebsiteDetailsWithMetaDataDTO websiteDetailsWithMetaDataDTO, boolean isRegister) {
        WebsiteDetails websiteDetails= new WebsiteDetails();
        WebsiteUserMetaData websiteUserMetaData;
        boolean isNewWebsite=true;

        String url= websiteDetailsWithMetaDataDTO.getUrl();
        if(url==null) throw  new CustomException(null, "error.empty.url", HttpStatus.BAD_REQUEST.value());

        url=url.replace("https:","http:");
        websiteDetails.setUrl(url);
        //if website already exists then just retrieve
        if(websiteDetailsRepository.findByUrl(url)==null){
            websiteDetailsRepository.save(websiteDetails);
        }else{
            isNewWebsite=false;
            websiteDetails= websiteDetailsRepository.findByUrl(url);
        }
        //find the user entity
        Optional<User> optionalUser=userRepository.findById(websiteDetailsWithMetaDataDTO.getUserId());
        if(optionalUser.isPresent()){
            User user=optionalUser.get();
            websiteUserMetaData = convertToWebsiteDetailsMetaData(websiteDetailsWithMetaDataDTO, websiteDetails);
            //for new website registration check if already exists
            if( user.getMappedWebsiteDetails() !=null && isRegister){
                //if user has some registered website check if this website is already mapped
                if(websiteUserMetaDataRepository.findById(new WebsiteUserMetaData_PK(user.getUserId(), websiteDetails.getWebId())).isPresent())
                    throw new CustomException(null, "error.already.registered", HttpStatus.BAD_REQUEST.value());

            }
            //else just save or update meta data
            websiteUserMetaDataRepository.save(websiteUserMetaData);

            //if new website is registered then schedule task for it.
            if(isNewWebsite)
                checkUptime.addForExecution(new WebsiteDetailsDTO(websiteDetails.getWebId(), websiteDetails.getUrl()));
            return env.getProperty("success.register.website");
        }else{
            throw new CustomException(null, "error.userNotExists", HttpStatus.BAD_REQUEST.value());
        }

    }


    @Override
    @Transactional
    public void updateUserWebsiteInfo(WebsiteDetailsWithMetaDataDTO websiteDetailsWithMetaDataDTO) {
        Optional<WebsiteUserMetaData> websiteUserMetaDataOptional= websiteUserMetaDataRepository.findById(
                new WebsiteUserMetaData_PK(websiteDetailsWithMetaDataDTO.getUserId(),
                        websiteDetailsWithMetaDataDTO.getWebId()));

        if(websiteUserMetaDataOptional.isPresent()){
            WebsiteUserMetaData existingWebsiteUserMetaData= websiteUserMetaDataOptional.get();
            saveWebsite(websiteDetailsWithMetaDataDTO,false );

            String existingUrl= existingWebsiteUserMetaData.getWebsiteDetails().getUrl();
            String updatedUrl= websiteDetailsWithMetaDataDTO.getUrl().replace("https:","http:");

            if(!existingUrl.equals(updatedUrl)){
                //if url is changed then delete old user website metadata
                removeWebsite(websiteDetailsWithMetaDataDTO.getUserId(), websiteDetailsWithMetaDataDTO.getWebId());
            }
        }
    }

    private static WebsiteUserMetaData convertToWebsiteDetailsMetaData(WebsiteDetailsWithMetaDataDTO websiteDetailsWithMetaData, WebsiteDetails websiteDetails) {
        WebsiteUserMetaData websiteUserMetaData;
        WebsiteUserMetaData_PK websiteUserMetaDataPk= new WebsiteUserMetaData_PK();
        websiteUserMetaData= new WebsiteUserMetaData();
        websiteUserMetaDataPk.setUserId(websiteDetailsWithMetaData.getUserId());
        websiteUserMetaDataPk.setWbId(websiteDetails.getWebId());
        websiteUserMetaData.setWebsiteUserMetaDataPk(websiteUserMetaDataPk);
        websiteUserMetaData.setWebsiteDetails(websiteDetails);
        websiteUserMetaData.setWebsiteName(websiteDetailsWithMetaData.getWebsiteName());
        return websiteUserMetaData;
    }



}
