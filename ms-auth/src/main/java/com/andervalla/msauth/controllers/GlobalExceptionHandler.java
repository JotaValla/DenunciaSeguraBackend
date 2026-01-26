package com.andervalla.msauth.controllers;

import com.andervalla.msauth.exceptions.CredentialNotFoundException;
import com.andervalla.msauth.exceptions.PasswordPolicyException;
import com.andervalla.msauth.exceptions.PasswordResetTokenExpiredException;
import com.andervalla.msauth.exceptions.PasswordResetTokenInvalidException;
import com.andervalla.msauth.exceptions.TokenHashingException;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Maneja errores comunes derivados de llamadas Feign hacia otros microservicios.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Traduce excepciones Feign a respuestas HTTP amigables.
     */
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<String> handleFeign(FeignException ex) {
        HttpStatus status = HttpStatus.resolve(ex.status());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        String body;
        if (status == HttpStatus.CONFLICT) {
            body = "Ya existe un usuario con los datos proporcionados.";
            log.warn("Conflicto al registrar usuario en ms-usuarios: status={} msg={}", status.value(), ex.getMessage());
        } else if (status.is4xxClientError()) {
            body = "Solicitud inválida al servicio de usuarios.";
            log.warn("Error 4xx desde ms-usuarios: status={} msg={}", status.value(), ex.getMessage());
        } else {
            body = "No se pudo completar la operación.";
            log.error("Error llamando a ms-usuarios: status={} msg={}", status.value(), ex.getMessage());
        }
        return ResponseEntity.status(status).body(body);
    }

    /**
     * Maneja violaciones de integridad locales (e.g., cédula duplicada en credenciales).
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Conflicto al persistir credencial: {}", ex.getMostSpecificCause().getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("Ya existe un usuario con los datos proporcionados.");
    }

    @ExceptionHandler(PasswordPolicyException.class)
    public ResponseEntity<String> handlePasswordPolicy(PasswordPolicyException ex) {
        log.warn("Contraseña inválida: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(CredentialNotFoundException.class)
    public ResponseEntity<String> handleCredentialNotFound(CredentialNotFoundException ex) {
        log.warn("Credencial no encontrada: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Credenciales no disponibles.");
    }

    @ExceptionHandler(PasswordResetTokenInvalidException.class)
    public ResponseEntity<String> handleInvalidToken(PasswordResetTokenInvalidException ex) {
        log.warn("Token de reset inválido.");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Token inválido o no encontrado.");
    }

    @ExceptionHandler(PasswordResetTokenExpiredException.class)
    public ResponseEntity<String> handleExpiredToken(PasswordResetTokenExpiredException ex) {
        log.warn("Token de reset expirado o usado.");
        return ResponseEntity.status(HttpStatus.GONE).body("Token expirado o ya usado.");
    }

    @ExceptionHandler(TokenHashingException.class)
    public ResponseEntity<String> handleTokenHashing(TokenHashingException ex) {
        log.error("Error al hashear token", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No se pudo procesar el token.");
    }
}
