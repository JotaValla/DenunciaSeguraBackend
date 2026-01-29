package com.andervalla.msauth.exceptions;

/**
 * Se lanza cuando el token de reseteo expiró o ya fue utilizado.
 */
public class PasswordResetTokenExpiredException extends RuntimeException {
    public PasswordResetTokenExpiredException(String message) {
        super(message);
    }
}
