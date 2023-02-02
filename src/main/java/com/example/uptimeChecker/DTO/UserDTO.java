package com.example.uptimeChecker.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Integer userId;
    private String userName;
    private String password;
    private Boolean enabled;
    private String email;
    private String slackId;

    public UserDTO(Integer userId, UpdateUserDTO updateUserDTO) {
        this.userId = userId;
        this.userName=updateUserDTO.getUserName();
        this.email=updateUserDTO.getEmail();
        this.slackId=updateUserDTO.getSlackId();
    }
}
