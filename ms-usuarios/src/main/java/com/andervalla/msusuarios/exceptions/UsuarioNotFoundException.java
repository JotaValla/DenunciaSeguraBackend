package com.andervalla.msusuarios.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UsuarioNotFoundException extends RuntimeException {
    public UsuarioNotFoundException(Long usuarioId) {
        super("Usuario no encontrado: " + usuarioId);
    }

    public UsuarioNotFoundException(String valor) {
        super("Usuario no encontrado: " + valor);
    }
}
