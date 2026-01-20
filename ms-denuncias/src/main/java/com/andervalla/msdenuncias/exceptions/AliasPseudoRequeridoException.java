package com.andervalla.msdenuncias.exceptions;

public class AliasPseudoRequeridoException extends RuntimeException {
    public AliasPseudoRequeridoException() {
        super("Alias publico requerido para denuncias en modo PSEUDOANONIMO");
    }
}
