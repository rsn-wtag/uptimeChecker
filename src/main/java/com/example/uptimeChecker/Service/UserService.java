package com.example.uptimeChecker.Service;

import com.example.uptimeChecker.DTO.UserDTO;
import com.example.uptimeChecker.DTO.UserSignUpRequestDTO;

public interface UserService {
    UserDTO updateUser(UserDTO userDTO);

    UserDTO getUserById(Integer userId);

    void saveUser(UserSignUpRequestDTO signUpRequest);
}
