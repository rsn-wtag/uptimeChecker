package com.example.uptimeChecker.Controller;

import com.example.uptimeChecker.Exceptions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {

    @Autowired
    Environment env;
    @ExceptionHandler(value = {ResourceNotFoundException.class})
    public ResponseEntity<ErrorMessage> resourceNotFoundException(ResourceNotFoundException ex) {
        ErrorMessage message = new ErrorMessage(
                new Date(),
                ex.getMessage(),
                env.getProperty(ex.getMessage()));

        return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = {BadCredentialsException.class})
    public ResponseEntity<ErrorMessage> authenticationException(BadCredentialsException ex) {
        ErrorMessage message = new ErrorMessage(
                new Date(),
                ex.getMessage(),
                env.getProperty("error.security.bad.credential"));

        return new ResponseEntity<>(message, HttpStatus.UNAUTHORIZED);
    }
    @ExceptionHandler(value = {UnauthorizedException.class})
    public ResponseEntity<ErrorMessage> unauthorizedException(UnauthorizedException ex) {
        ErrorMessage message = new ErrorMessage(
                new Date(),
                ex.getMessage(),
                env.getProperty(ex.getMessage()));

        return new ResponseEntity<>(message, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(value = {BindException.class})
    public ResponseEntity<ErrorMessage> beanValidationException(BindException ex) {

        List<FieldError> fieldErrorList = new ArrayList<>();
        ex.getFieldErrors().forEach(fieldError -> {
            fieldErrorList.add(new FieldError(fieldError));
        });
        UnprocessableEntityErrorMessage message = new UnprocessableEntityErrorMessage(
                new Date(),
                ex.getMessage(),
                env.getProperty("error.binding.error"),
                fieldErrorList);

        return new ResponseEntity<>(message, HttpStatus.UNPROCESSABLE_ENTITY);
    }
    @ExceptionHandler(value = {UnprocessableEntityException.class})
    public ResponseEntity<ErrorMessage> unprocessableEntityException(UnprocessableEntityException ex){
        UnprocessableEntityErrorMessage message = new UnprocessableEntityErrorMessage(
                new Date(),
                ex.getMessage(),
                env.getProperty(ex.getMessage()),
                null);

        return new ResponseEntity<>(message, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<ErrorMessage> serverException(Exception ex) {
        ErrorMessage message = new ErrorMessage(
                new Date(),
                ex.getMessage(),
                env.getProperty("error.message"));

        return new ResponseEntity<>(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}