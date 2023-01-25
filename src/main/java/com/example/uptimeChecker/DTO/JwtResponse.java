package com.example.uptimeChecker.DTO;

import com.example.uptimeChecker.Entities.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class JwtResponse {
    private String jwtToken;

    private Integer userId;
    private String userName;
    private List<String> roles;


}
