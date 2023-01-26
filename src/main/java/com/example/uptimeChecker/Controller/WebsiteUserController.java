package com.example.uptimeChecker.Controller;

import com.example.uptimeChecker.DTO.DownTimeDTO;
import com.example.uptimeChecker.DTO.DownTimeSummaryDTO;
import com.example.uptimeChecker.DTO.WebsiteDetailsWithMetaDataDTO;
import com.example.uptimeChecker.Service.DowntimeService;
import com.example.uptimeChecker.Service.WebsiteUserService;
import com.example.uptimeChecker.constants.RestEndpoints;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Set;


@RestController
public class WebsiteUserController {
    @Autowired
    private WebsiteUserService websiteUserService;
    @Autowired
    private DowntimeService downtimeService;


    @GetMapping(RestEndpoints.USER_WEBSITE_LIST)
    public ResponseEntity<Set<WebsiteDetailsWithMetaDataDTO>> getWebsitesByUser(@PathVariable Integer userId){
        return  ResponseEntity.status(HttpStatus.OK).body(websiteUserService.getWebsiteDetailsByUser(userId));
    }
    @RequestMapping(RestEndpoints.REGISTER_WEBSITE)
    public ResponseEntity<?> registerWebsite(@Valid @RequestBody WebsiteDetailsWithMetaDataDTO websiteDetailsWithMetaData){
        websiteUserService.saveWebsite(websiteDetailsWithMetaData,true );
        return ResponseEntity.ok().build() ;
    }
    @DeleteMapping(RestEndpoints.REMOVE_WEBSITE)
    public ResponseEntity<?> removeWebsite(@PathVariable Integer userId, @PathVariable Integer webId){
        websiteUserService.removeWebsite(userId,webId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }


    @GetMapping(RestEndpoints.USER_WEBSITE_INFO)
    public ResponseEntity<WebsiteDetailsWithMetaDataDTO> getWebsite(@PathVariable Integer userId, @PathVariable Integer webId){
        return  ResponseEntity.status(HttpStatus.OK).body(websiteUserService.getWebsite(userId, webId));
    }

    @RequestMapping(RestEndpoints.UPDATE_WEBSITE)
    public ResponseEntity<?> updateWebsite(@Valid @RequestBody WebsiteDetailsWithMetaDataDTO websiteDetails){
        websiteUserService.updateUserWebsiteInfo(websiteDetails);
        return ResponseEntity.ok().build() ;
    }

    @GetMapping(RestEndpoints.WEBSITE_DOWNTIME_HISTORY)
    public ResponseEntity<Set<DownTimeSummaryDTO>> getWebsiteDayWiseDownTimeHistory(@PathVariable Integer webId){
        return  ResponseEntity.status(HttpStatus.OK).body(downtimeService.getDayWiseDownTimeHistory(webId));
    }

    @GetMapping(RestEndpoints.WEBSITE_DOWNTIME_HISTORY_TODAY)
    public ResponseEntity<Set<DownTimeDTO>> getWebsiteDownTimeHistoryToday(@PathVariable Integer webId){
        return  ResponseEntity.status(HttpStatus.OK).body(downtimeService.getTodayDownTimeHistory(webId));
    }

}
