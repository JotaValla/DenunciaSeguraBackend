package com.andervalla.msauth.exceptions;

/**
 * Se lanza cuando el token de reseteo no existe o es inválido.
 */
public class PasswordResetTokenInvalidException extends RuntimeException {
    public PasswordResetTokenInvalidException(String message) {
        super(message);
    }
}
