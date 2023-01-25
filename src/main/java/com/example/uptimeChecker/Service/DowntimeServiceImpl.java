package com.example.uptimeChecker.Service;

import com.example.uptimeChecker.DTO.WebsiteDetailsDTO;
import com.example.uptimeChecker.Entities.Downtime;
import com.example.uptimeChecker.Exceptions.CustomException;
import com.example.uptimeChecker.Repositories.DowntimeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class DowntimeServiceImpl implements DowntimeService {
    @Autowired
    DowntimeRepository downtimeRepository;
    @Value("${max.fail.count}")
    Integer maxFailCount;
    public void saveDowntimeInfo(WebsiteDetailsDTO websiteDetailsDTO) {
        try {
            if (websiteDetailsDTO.getTotalConsecutiveFailCount() == maxFailCount) {
                Downtime downtime = new Downtime(websiteDetailsDTO.getWebId(), OffsetDateTime.now());
                downtimeRepository.save(downtime);
            }

        } catch (Exception e) {
            throw new CustomException(e.getMessage(), "unableToSave.message");
        }

    }

    public void updateDowntimeInfo(WebsiteDetailsDTO websiteDetailsDTO){
        try {
            Downtime downtime= downtimeRepository.findFirstByWebIdOrderByDownTimeIdDesc(websiteDetailsDTO.getWebId());
            if (downtime!=null && downtime.getEndTime()==null) {
                downtime.setEndTime(OffsetDateTime.now());
                downtimeRepository.save(downtime);
            }

        } catch (Exception e) {
            throw new CustomException(e.getMessage(), "unableToUpdate.message");
        }
    }

}
