package com.example.uptimeChecker.Exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FieldError {
    private  String field;

    private  Object rejectedValue;

    private  boolean bindingFailure;

    private String fieldErrorMessage;

    public FieldError(org.springframework.validation.FieldError fieldError) {
        this.field=fieldError.getField();
        this.rejectedValue=fieldError.getRejectedValue();
        this.bindingFailure=fieldError.isBindingFailure();
        this.fieldErrorMessage= fieldError.getDefaultMessage();
    }

    public FieldError(String field, Object rejectedValue, String fieldErrorMessage) {
        this.field = field;
        this.rejectedValue = rejectedValue;
        this.fieldErrorMessage = fieldErrorMessage;
    }
}
