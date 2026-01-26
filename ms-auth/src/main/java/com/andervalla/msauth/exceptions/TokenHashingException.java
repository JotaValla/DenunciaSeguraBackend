package com.andervalla.msauth.exceptions;

/**
 * Se lanza cuando ocurre un error al hashear valores sensibles.
 */
public class TokenHashingException extends RuntimeException {
    public TokenHashingException(String message, Throwable cause) {
        super(message, cause);
    }
}
