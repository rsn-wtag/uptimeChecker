package com.example.uptimeChecker.Exceptions;

import lombok.Getter;

@Getter
public class CustomException extends  RuntimeException{
    String customMessageCode;
    int errorCode;
    public CustomException(String msg, String customMessageCode){
        super(msg);
        this.customMessageCode= customMessageCode;
        this.errorCode=500;
    }

    public CustomException(String msg, String customMessageCode, int errorCode){
        super(msg);
        this.customMessageCode= customMessageCode;
        this.errorCode=errorCode;
    }

    public CustomException(String customMessageCode, int errorCode){
        super(customMessageCode);
        this.customMessageCode= customMessageCode;
        this.errorCode=errorCode;
    }


}
