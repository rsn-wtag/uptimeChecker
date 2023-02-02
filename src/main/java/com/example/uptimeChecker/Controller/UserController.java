package com.example.uptimeChecker.Controller;

import com.example.uptimeChecker.DTO.UpdateUserDTO;
import com.example.uptimeChecker.DTO.UserDTO;
import com.example.uptimeChecker.DTO.UserDetailsImpl;
import com.example.uptimeChecker.Service.UserService;
import com.example.uptimeChecker.Constants.RestEndpoints;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {
    @Autowired
    private UserService userService;
    @PreAuthorize("#userId== authentication.principal.userId")
    @PatchMapping(RestEndpoints.UPDATE_USER)
    public ResponseEntity<UserDTO> updateUser(@PathVariable Integer userId,@RequestBody UpdateUserDTO user){
        return ResponseEntity.ok(userService.updateUser(new UserDTO(userId,user)));
    }
}
