package com.turkcell.crm.identityService.core.utilities.exceptions.types;

public class BusinessException extends RuntimeException{
    public BusinessException(String message){
        super(message);
    }
}