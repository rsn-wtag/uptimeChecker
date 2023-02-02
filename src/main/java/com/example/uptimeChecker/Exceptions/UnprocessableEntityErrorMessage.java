package com.example.uptimeChecker.Exceptions;

import lombok.*;

import java.util.Date;
import java.util.List;


@Getter
@Setter
public class UnprocessableEntityErrorMessage extends ErrorMessage{
    private List<FieldError>  fieldErrors;
    public UnprocessableEntityErrorMessage(Date timestamp, String errorMessage, String customMessage, List<FieldError> fieldErrors) {
        super( timestamp, errorMessage, customMessage);
        this.fieldErrors=fieldErrors;
    }

}
