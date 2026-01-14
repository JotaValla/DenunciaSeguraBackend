package com.andervalla.msusuarios.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class AliasPublicoInvalidoException extends RuntimeException {
    public AliasPublicoInvalidoException(String message) {
        super(message);
    }
}
