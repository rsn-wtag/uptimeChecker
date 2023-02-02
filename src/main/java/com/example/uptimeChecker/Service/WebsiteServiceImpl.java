package com.example.uptimeChecker.Service;

import com.example.uptimeChecker.DTO.WebsiteDetailsDTO;
import com.example.uptimeChecker.Entities.WebsiteDetails;
import com.example.uptimeChecker.Repositories.WebsiteDetailsRepository;
import com.googlecode.jmapper.JMapper;
import com.googlecode.jmapper.api.JMapperAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.googlecode.jmapper.api.JMapperAPI.attribute;
import static com.googlecode.jmapper.api.JMapperAPI.mappedClass;

@Service
public class WebsiteServiceImpl implements WebsiteService {
    @Autowired
    WebsiteDetailsRepository websiteDetailsRepository;
    public List<WebsiteDetailsDTO> getWesiteDetailList() {
        List<WebsiteDetailsDTO> websiteDetailsDTOList = new ArrayList<>();

        JMapper<WebsiteDetailsDTO, WebsiteDetails> mapper = getWebsiteDetailsDTOMapper();
        WebsiteDetailsDTO websiteDetailsDTO;
        for (WebsiteDetails websiteDetails : websiteDetailsRepository.findAll()) {
            websiteDetailsDTO = mapper.getDestination(websiteDetails);
            websiteDetailsDTOList.add(websiteDetailsDTO);
        }

        return websiteDetailsDTOList;
    }
    @Override
    public boolean isAnyWebsiteExists(){
       return websiteDetailsRepository.count()>0;

    }
    private JMapper getWebsiteDetailsDTOMapper() {
        JMapperAPI jmapperApi = new JMapperAPI()
                .add(mappedClass(WebsiteDetailsDTO.class)
                        .add(attribute("webId").value("webId"))
                        .add(attribute("url").value("url")));
        JMapper<WebsiteDetailsDTO, WebsiteDetails> mapper = new JMapper<>(WebsiteDetailsDTO.class, WebsiteDetails.class, jmapperApi);
        return mapper;
    }

    private JMapper getWebsiteDetailsMapper() {
        JMapperAPI jmapperApi = new JMapperAPI()
                .add(mappedClass(WebsiteDetails.class)
                        .add(attribute("webId").value("webId"))
                        .add(attribute("url").value("url")));
        return new JMapper<>(WebsiteDetails.class, WebsiteDetailsDTO.class, jmapperApi);

    }



}
