package com.andervalla.msauth.exceptions;

/**
 * Se lanza cuando una contraseña no cumple las políticas definidas.
 */
public class PasswordPolicyException extends RuntimeException {
    public PasswordPolicyException(String message) {
        super(message);
    }
}
