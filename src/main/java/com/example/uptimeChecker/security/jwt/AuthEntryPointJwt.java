package com.example.uptimeChecker.security.jwt;

import com.example.uptimeChecker.Exceptions.ErrorMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

/*
* handle unauthorizes user
* */
@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {
    private static final Logger logger = LoggerFactory.getLogger(AuthEntryPointJwt.class);
    @Autowired
    Environment environment;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        logger.error("Unauthorized error: {}", authException.getMessage());
        ObjectMapper mapper= new ObjectMapper();
      //  response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error: Unauthorized");
        ErrorMessage errorMessage= new ErrorMessage( new Date(), authException.getMessage(),
                environment.getProperty("error.security.unauthorized"));
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(mapper.writeValueAsString(errorMessage));

    }
}
