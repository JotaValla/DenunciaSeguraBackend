package com.andervalla.msauth.exceptions;

/**
 * Se lanza cuando no se encuentra la credencial asociada a un usuario.
 */
public class CredentialNotFoundException extends RuntimeException {
    public CredentialNotFoundException(String message) {
        super(message);
    }
}
