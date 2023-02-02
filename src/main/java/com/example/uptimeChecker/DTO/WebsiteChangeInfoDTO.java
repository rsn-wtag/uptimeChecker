package com.example.uptimeChecker.DTO;

import com.example.uptimeChecker.Enums.WebsiteChangeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WebsiteChangeInfoDTO {
    private WebsiteChangeType websiteChangeType;
    private WebsiteDetailsDTO websiteDetailsDTO;

}
