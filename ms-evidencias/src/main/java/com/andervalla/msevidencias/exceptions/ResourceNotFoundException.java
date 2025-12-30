package com.andervalla.msevidencias.exceptions;

public class ResourceNotFoundException extends  RuntimeException{
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
