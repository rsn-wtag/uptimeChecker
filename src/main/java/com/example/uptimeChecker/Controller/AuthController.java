package com.example.uptimeChecker.Controller;

import com.example.uptimeChecker.DTO.JwtResponse;
import com.example.uptimeChecker.DTO.UserDetailsImpl;
import com.example.uptimeChecker.DTO.UserLoginRequestDTO;
import com.example.uptimeChecker.DTO.UserSignUpRequestDTO;
import com.example.uptimeChecker.Entities.Role;
import com.example.uptimeChecker.Entities.User;
import com.example.uptimeChecker.Exceptions.CustomException;
import com.example.uptimeChecker.Exceptions.ErrorMessage;
import com.example.uptimeChecker.Repositories.UserRepository;
import com.example.uptimeChecker.Util.CookieUtil;
import com.example.uptimeChecker.security.jwt.JwtUtils;
import com.sun.xml.internal.fastinfoset.util.CharArray;
import org.hibernate.validator.internal.constraintvalidators.bv.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private CookieUtil cookieUtil;

    @Value("${authentication.token.cookie.name}")
    private String cookieName;
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody UserLoginRequestDTO loginRequestDTO) {


        Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequestDTO.getUsername(), String.valueOf(loginRequestDTO.getPassword())));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        HttpHeaders httpHeaders= new HttpHeaders();
        httpHeaders.add(HttpHeaders.SET_COOKIE,cookieUtil.createCookie(jwt).toString());
        return ResponseEntity.ok().headers(httpHeaders).body(new JwtResponse(jwt,
                userDetails.getUserId(),
                userDetails.getUsername(),
                roles));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserSignUpRequestDTO signUpRequest) {

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
        return authenticateUser(new UserLoginRequestDTO(signUpRequest.getUsername(), signUpRequest.getPassword()));
        //return ResponseEntity.ok("User registered successfully!");
    }

    @GetMapping("/logout")
    public ResponseEntity<?> Logout(HttpServletRequest request, HttpServletResponse response){

            HttpSession session = request.getSession(false);

            if (session != null) {
                session.invalidate();
            }

            if (request.getCookies() != null) {
                System.out.println("i found some cookies");
                for (Cookie cookie : request.getCookies()) {
                    if(cookie.getName().equals(cookieName)){
                        cookie.setMaxAge(0);
                        cookie.setValue("");
                        cookie.setHttpOnly(true);
                        cookie.setPath("/");
                        response.addCookie(cookie);
                    }
                }
            }




            return ResponseEntity.ok().body("");
    }

}
