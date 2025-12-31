package com.andervalla.msevidencias.controllers;

import com.andervalla.msevidencias.controllers.dtos.responses.ErrorResponse;
import com.andervalla.msevidencias.exceptions.EvidenciaEstadoInvalidoException;
import com.andervalla.msevidencias.exceptions.EvidenciaYaAsociadaException;
import com.andervalla.msevidencias.exceptions.ResourceNotFoundException;
import com.andervalla.msevidencias.exceptions.StorageException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex,
                                                        HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<ErrorResponse> handleStorage(StorageException ex,
                                                       HttpServletRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler({
            EvidenciaEstadoInvalidoException.class,
            EvidenciaYaAsociadaException.class
    })
    public ResponseEntity<ErrorResponse> handleConflict(RuntimeException ex,
                                                        HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            ConstraintViolationException.class,
            MissingServletRequestParameterException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception ex, HttpServletRequest request) {
        String message = resolveBadRequestMessage(ex);
        return buildResponse(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
    }

    @ExceptionHandler({SecurityException.class, RuntimeException.class})
    public ResponseEntity<ErrorResponse> handleSecurity(RuntimeException ex,
                                                        HttpServletRequest request) {
        if (ex instanceof SecurityException || isSecurityExceptionMessage(ex.getMessage())) {
            String message = ex.getMessage() != null ? ex.getMessage() : "Solicitud invalida";
            return buildResponse(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno del servidor",
                request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno del servidor",
                request.getRequestURI());
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message, String path) {
        ErrorResponse error = new ErrorResponse(
                Instant.now().toString(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path
        );
        return ResponseEntity.status(status).body(error);
    }

    private String resolveBadRequestMessage(Exception ex) {
        if (ex instanceof MethodArgumentNotValidException manve) {
            FieldError fieldError = manve.getBindingResult().getFieldError();
            if (fieldError != null) {
                return "Campo " + fieldError.getField() + ": " + fieldError.getDefaultMessage();
            }
            return "Solicitud invalida";
        }
        if (ex instanceof ConstraintViolationException cve && !cve.getConstraintViolations().isEmpty()) {
            return cve.getConstraintViolations().iterator().next().getMessage();
        }
        if (ex instanceof MissingServletRequestParameterException msrpe) {
            return "Parametro requerido ausente: " + msrpe.getParameterName();
        }
        if (ex instanceof HttpMessageNotReadableException) {
            return "Cuerpo de la solicitud invalido o mal formado";
        }
        return ex.getMessage() != null ? ex.getMessage() : "Solicitud invalida";
    }

    private boolean isSecurityExceptionMessage(String message) {
        if (message == null || message.isBlank()) {
            return false;
        }
        String normalized = message.toLowerCase();
        return normalized.contains("seguridad") || normalized.contains("magic bytes");
    }
}
