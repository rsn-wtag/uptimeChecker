package com.example.uptimeChecker.Service;

import com.example.uptimeChecker.DTO.UserDetailsImpl;
import com.example.uptimeChecker.Entities.User;
import com.example.uptimeChecker.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user= userRepository.findUserByUserName(username);
        if(user==null)
            throw new UsernameNotFoundException("User Not Found");

        return  UserDetailsImpl.build(user);
    }
}
