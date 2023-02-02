package com.example.uptimeChecker.Service;

import com.example.uptimeChecker.DTO.*;
import com.example.uptimeChecker.Entities.User;
import com.example.uptimeChecker.Entities.WebsiteDetails;
import com.example.uptimeChecker.Entities.WebsiteUserMetaData;
import com.example.uptimeChecker.Entities.WebsiteUserMetaData_PK;
import com.example.uptimeChecker.Exceptions.CustomException;
import com.example.uptimeChecker.Exceptions.ResourceNotFoundException;
import com.example.uptimeChecker.Exceptions.UnprocessableEntityException;
import com.example.uptimeChecker.Repositories.UserRepository;
import com.example.uptimeChecker.Repositories.WebsiteDetailsRepository;
import com.example.uptimeChecker.Repositories.WebsiteUserMetaDataRepository;
import com.example.uptimeChecker.Enums.WebsiteChangeType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.BlockingQueue;


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
    private BlockingQueue<WebsiteChangeInfoDTO> websiteChangeTrackerQueue;


    public Set<WebsiteDetailsWithMetaDataDTO> getWebsiteDetailsByUser(Integer userId) {
        Set<WebsiteDetailsWithMetaDataDTO> websiteSet= new HashSet<>();
        WebsiteDetailsWithMetaDataDTO websiteDetailsWithMetaDataDTO;

            Optional<User> optionalUser = userRepository.findById(userId);

            if (optionalUser.isPresent()) {
                for(WebsiteUserMetaData websiteUserMetaData : optionalUser.get().getMappedWebsiteDetails()){
                    websiteDetailsWithMetaDataDTO= new WebsiteDetailsWithMetaDataDTO(websiteUserMetaData.getWebsiteDetails().getWebId(),
                            websiteUserMetaData.getWebsiteDetails().getUrl(),websiteUserMetaData.getWebsiteName());
                    websiteSet.add(websiteDetailsWithMetaDataDTO);
                }
            } else {
                throw new ResourceNotFoundException("user.not.found");
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
                  cancelTaskForRemovedWebsite(websiteDetails);
                }
            }

        }
    }
    private void cancelTaskForRemovedWebsite(WebsiteDetails websiteDetails){
        WebsiteDetailsDTO websiteDetailsDTO= new WebsiteDetailsDTO();
        websiteDetailsDTO.setWebId(websiteDetails.getWebId());
        websiteChangeTrackerQueue.add(new WebsiteChangeInfoDTO(WebsiteChangeType.DELETE,websiteDetailsDTO));
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
           throw new ResourceNotFoundException("error.website.not.found");
       }
    }
    @Override
    @Transactional
    public void saveWebsite(WebsiteDetailsWithMetaDataDTO websiteDetailsWithMetaDataDTO, boolean isRegister) {
        UserDetailsImpl userDetails=(UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        websiteDetailsWithMetaDataDTO.setUserId(userDetails.getUserId());
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
                  throw new UnprocessableEntityException("error.already.registered");
                  //  throw new CustomException( "error.already.registered", HttpStatus.BAD_REQUEST.value());

            }
            //else just save or update meta data
            websiteUserMetaDataRepository.save(websiteUserMetaData);

            //if new website is registered then schedule task for it.
            if(isNewWebsite)
                websiteChangeTrackerQueue.add(new WebsiteChangeInfoDTO(WebsiteChangeType.ADD,
                        new WebsiteDetailsDTO(websiteDetails.getWebId(), websiteDetails.getUrl())));
        }else{
            throw new ResourceNotFoundException("error.userNotExists");
        }

    }

    /*
    * if the url of the website is updated then simply register new website and map it to user.
    * otherwise just update the website user mapping with metadata
    * */
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
    @Transactional
    @Override
    public Set<UserDTO> getUsersByWebsite(Integer webId) {
        Optional<WebsiteDetails> websiteDetailsOptional= websiteDetailsRepository.findById(webId);
        Set<UserDTO> userDTOSet= new HashSet<>();
        if(websiteDetailsOptional.isPresent()){
            WebsiteDetails websiteDetails= websiteDetailsOptional.get();
            Set<WebsiteUserMetaData> websiteUserMetaDataSet= websiteDetails.getWebsiteUserMetaDataSet();
            for(WebsiteUserMetaData websiteUserMetaData:websiteUserMetaDataSet){
                User user=  websiteUserMetaData.getUser();
                userDTOSet.add(new UserDTO(user.getUserId(), user.getUserName(), "", true, user.getEmail(), user.getSlackId()));
            }
        }

        return userDTOSet;
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
