package com.example.uptimeChecker.Controller;

import com.example.uptimeChecker.DTO.DownTimeDTO;
import com.example.uptimeChecker.DTO.DownTimeSummaryDTO;
import com.example.uptimeChecker.DTO.UserDetailsImpl;
import com.example.uptimeChecker.DTO.WebsiteDetailsWithMetaDataDTO;
import com.example.uptimeChecker.DTO.website.WebsiteDTO;
import com.example.uptimeChecker.Service.DowntimeService;
import com.example.uptimeChecker.Service.WebsiteUserService;
import com.example.uptimeChecker.Constants.RestEndpoints;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;


@RestController
public class WebsiteUserController {
    @Autowired
    private WebsiteUserService websiteUserService;
    @Autowired
    private DowntimeService downtimeService;

    //@PreAuthorize("#userId== authentication.principal.userId")
    @PostMapping(RestEndpoints.REGISTER_WEBSITE)
    public ResponseEntity<?> registerWebsite(@Valid @RequestBody WebsiteDTO registerWebsite){
        UserDetailsImpl userDetails= (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        websiteUserService.saveWebsite(new WebsiteDetailsWithMetaDataDTO(registerWebsite, userDetails.getUserId()),true );
        return ResponseEntity.status(HttpStatus.CREATED).build() ;
    }

    @GetMapping(RestEndpoints.WEBSITE_LIST)
    public ResponseEntity<Set<WebsiteDetailsWithMetaDataDTO>> getWebsitesByUser(){
        UserDetailsImpl userDetails=(UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return  ResponseEntity.ok().body(websiteUserService.getWebsiteDetailsByUser(userDetails.getUserId()));
    }

    //@PreAuthorize("#userId== authentication.principal.userId")
    @PatchMapping(RestEndpoints.UPDATE_WEBSITE)
    public ResponseEntity<?> updateWebsite(@PathVariable Integer webId, @Valid @RequestBody WebsiteDTO websiteDTO){
        UserDetailsImpl userDetails=(UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        websiteUserService.updateUserWebsiteInfo(new WebsiteDetailsWithMetaDataDTO(websiteDTO,webId,userDetails.getUserId()));
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build() ;
    }

    @DeleteMapping(RestEndpoints.REMOVE_WEBSITE)
    public ResponseEntity<Void> removeWebsite(@PathVariable Integer webId){
        UserDetailsImpl userDetails=(UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        websiteUserService.removeWebsite(userDetails.getUserId(),webId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping(RestEndpoints.WEBSITE_INFO)
    public ResponseEntity<WebsiteDetailsWithMetaDataDTO> getWebsite(@PathVariable Integer webId){
        UserDetailsImpl userDetails=(UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return  ResponseEntity.status(HttpStatus.OK).body(websiteUserService.getWebsite(userDetails.getUserId(), webId));
    }
    @GetMapping(RestEndpoints.WEBSITE_DOWNTIME_HISTORY)
    public ResponseEntity<List<DownTimeSummaryDTO>> getWebsiteDayWiseDownTimeHistory(@PathVariable Integer webId){
        return  ResponseEntity.status(HttpStatus.OK).body(downtimeService.getDayWiseDownTimeHistory(webId));
    }

    @GetMapping(RestEndpoints.WEBSITE_DOWNTIME_HISTORY_TODAY)
    public ResponseEntity<List<DownTimeDTO>> getWebsiteDownTimeHistoryToday(@PathVariable Integer webId){
        return  ResponseEntity.status(HttpStatus.OK).body(downtimeService.getTodayDownTimeHistory(webId));
    }

}
