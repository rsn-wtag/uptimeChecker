package com.example.uptimeChecker.Controller;

import com.example.uptimeChecker.Exceptions.CustomException;
import com.example.uptimeChecker.Exceptions.ErrorMessage;
import com.example.uptimeChecker.Exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Date;

@ControllerAdvice
public class GlobalExceptionHandler {

    @Autowired
    Environment env;
    @ExceptionHandler(value = {ResourceNotFoundException.class})
    public ResponseEntity<ErrorMessage> resourceNotFoundException(ResourceNotFoundException ex) {
        ErrorMessage message = new ErrorMessage(
                404,
                new Date(),
                ex.getMessage(),
                "");

        return new ResponseEntity<ErrorMessage>(message, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = {CustomException.class})
    public ResponseEntity<ErrorMessage> CustomException(CustomException ex) {

        ErrorMessage message = new ErrorMessage(
                ex.getErrorCode(),
                new Date(),
                ex.getMessage(),
                env.getProperty(ex.getCustomMessageCode()));

        return new ResponseEntity<ErrorMessage>(message, HttpStatus.valueOf(ex.getErrorCode()));
    }
    @ExceptionHandler(value = {BadCredentialsException.class})
    public ResponseEntity<ErrorMessage> authenticationException(BadCredentialsException ex) {
        ErrorMessage message = new ErrorMessage(
                HttpStatus.UNAUTHORIZED.value(),
                new Date(),
                ex.getMessage(),
                env.getProperty("error.security.bad.credential"));

        return new ResponseEntity<ErrorMessage>(message, HttpStatus.UNAUTHORIZED);
    }
    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<ErrorMessage> serverException(Exception ex) {
        ErrorMessage message = new ErrorMessage(
                500,
                new Date(),
                ex.getMessage(),
                env.getProperty("error.message"));

        return new ResponseEntity<ErrorMessage>(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}