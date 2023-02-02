package com.example.uptimeChecker.Exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UnprocessableEntityException extends RuntimeException{
    public UnprocessableEntityException(String message) {
        super(message);
    }


}
