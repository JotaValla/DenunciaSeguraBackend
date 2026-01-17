package com.andervalla.msauth.controllers;

import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<String> handleFeign(FeignException ex) {
        HttpStatus status = HttpStatus.resolve(ex.status());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        String body;
        if (status == HttpStatus.CONFLICT) {
            body = "Ya existe un usuario con los datos proporcionados.";
        } else if (status.is4xxClientError()) {
            body = "Solicitud inválida al servicio de usuarios.";
        } else {
            body = "No se pudo completar la operación.";
        }
        return ResponseEntity.status(status).body(body);
    }
}
