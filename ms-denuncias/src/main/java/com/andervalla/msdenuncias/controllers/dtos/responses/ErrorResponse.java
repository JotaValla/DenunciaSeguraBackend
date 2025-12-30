package com.andervalla.msdenuncias.controllers.dtos.responses;

public record ErrorResponse(
        String timestamp,
        int status,
        String error,
        String message,
        String path
) {
}
