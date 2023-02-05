package com.example.uptimeChecker.Controller;

import com.example.uptimeChecker.DTO.*;
import com.example.uptimeChecker.Service.UserService;
import com.example.uptimeChecker.Util.CookieUtil;
import com.example.uptimeChecker.security.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

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

        UserDTO userDTO= userService.getUserById(userDetails.getUserId());
        HttpHeaders httpHeaders= new HttpHeaders();
        httpHeaders.add(HttpHeaders.SET_COOKIE,cookieUtil.createCookie(jwt).toString());
        return ResponseEntity.ok().headers(httpHeaders).body(new JwtResponse(jwt,
               userDTO,
                roles));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserSignUpRequestDTO signUpRequest) {
        userService.saveUser(signUpRequest);
        return authenticateUser(new UserLoginRequestDTO(signUpRequest.getUsername(), signUpRequest.getPassword()));

    }

    @GetMapping("/logout")
    public ResponseEntity<?> Logout(HttpServletRequest request, HttpServletResponse response){

            HttpSession session = request.getSession(false);

            if (session != null) {
                session.invalidate();
            }

            if (request.getCookies() != null) {

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
