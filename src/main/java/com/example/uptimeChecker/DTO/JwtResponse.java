package com.example.uptimeChecker.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class JwtResponse {
    private String jwtToken;
    private UserDTO userDTO;
    private List<String> roles;


}
