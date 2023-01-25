package com.example.uptimeChecker.Util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;


@Component
public class CookieUtil {

    @Value("${authentication.token.cookie.name}")
    private String cookieName;


    public HttpCookie createCookie(String jwtToken){
        return ResponseCookie.from(cookieName, jwtToken)
                .maxAge(-1)
                .httpOnly(true)
                .path("/")
                .build();
    }



}
