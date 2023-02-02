package com.example.uptimeChecker.Service;

import com.example.uptimeChecker.DTO.UserDTO;
import com.example.uptimeChecker.DTO.UserDetailsImpl;
import com.example.uptimeChecker.DTO.UserSignUpRequestDTO;
import com.example.uptimeChecker.Entities.Role;
import com.example.uptimeChecker.Entities.User;
import com.example.uptimeChecker.Exceptions.CustomException;
import com.example.uptimeChecker.Exceptions.ResourceNotFoundException;
import com.example.uptimeChecker.Exceptions.UnauthorizedException;
import com.example.uptimeChecker.Exceptions.UnprocessableEntityException;
import com.example.uptimeChecker.Repositories.UserRepository;
import com.sun.xml.internal.fastinfoset.util.CharArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;
    @Override
    public UserDTO updateUser(UserDTO userDTO) {
        UserDetailsImpl userDetails=(UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<User> userOptional= userRepository.findById(userDTO.getUserId());
        if(userOptional.isPresent()){
            User user= userOptional.get();
            if(!user.getUserId().equals(userDetails.getUserId())){
                throw new UnauthorizedException("error.unauthorized.update");
            }
            if (!userDTO.getUserName().trim().equals(user.getUserName()) && userRepository.existsUserByUserName(userDTO.getUserName())) {
                throw new UnprocessableEntityException("error.user.name.exists");
            }

            user.setUserName(userDTO.getUserName());
            user.setEmail(userDTO.getEmail());
            user.setSlackId(userDTO.getSlackId());

            userRepository.save(user);
            return new UserDTO(user.getUserId(), user.getUserName(),"",user.getEnabled(), user.getEmail(), user.getSlackId());
        }else{
            throw new ResourceNotFoundException( "error.user.not.found");
        }

    }

    @Override
    public UserDTO getUserById(Integer userId){
        Optional<User> userOptional= userRepository.findById(userId);
        if(userOptional.isPresent()){
            User user= userOptional.get();
            return new UserDTO(userId,user.getUserName(), "", user.getEnabled(), user.getEmail(), user.getSlackId());
        }else{
            throw new ResourceNotFoundException("error.user.not.found");
        }
    }

    @Override
    public void saveUser(UserSignUpRequestDTO signUpRequest){
        if (userRepository.existsUserByUserName(signUpRequest.getUsername())) {
            throw new CustomException("error.user.name.exists", HttpStatus.BAD_REQUEST.value());
        }
        if (userRepository.existsUserByEmail(signUpRequest.getEmail())) {
            throw new CustomException("error.email.exists", HttpStatus.BAD_REQUEST.value());
        }

        User user = new User(signUpRequest.getUsername(),
                passwordEncoder.encode(
                        new CharArray(signUpRequest.getPassword(), 0,
                                signUpRequest.getPassword().length, true)), true,
                signUpRequest.getEmail(), signUpRequest.getSlackId()
        );

        //  Set<String> strRoles = signUpRequest.get();
        Set<Role> roles = new HashSet<>();

        // if (strRoles == null) {
        Role userRole = new Role(1, "User");
        roles.add(userRole);
    /*    } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);

                        break;
                    case "mod":
                        Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(modRole);

                        break;
                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                }
            });
        }*/

        //  user.setRoles(roles);
        userRepository.save(user);
    }
}
