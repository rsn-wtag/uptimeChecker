package com.example.uptimeChecker.DTO.website;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WebsiteDTO {
    @NotNull
    private String url;
    private String websiteName;
}
