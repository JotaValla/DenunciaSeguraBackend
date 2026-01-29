package com.andervalla.msauth.controllers;

import com.andervalla.msauth.controllers.dtos.request.PasswordForgotRequest;
import com.andervalla.msauth.controllers.dtos.request.PasswordResetRequest;
import com.andervalla.msauth.controllers.dtos.response.PasswordResetResponse;
import com.andervalla.msauth.services.auth.PasswordResetService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints para iniciar y completar el reseteo de contraseñas.
 */
@RestController
@RequestMapping("/password")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    /**
     * Constructor para inyectar el servicio de reseteo.
     */
    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    /**
     * Genera un token de reseteo para la cédula indicada.
     */
    @PostMapping("/forgot")
    @ResponseStatus(HttpStatus.CREATED)
    public PasswordResetResponse forgot(@RequestBody PasswordForgotRequest request) {
        String token = passwordResetService.generateResetToken(request.cedula());
        // En un entorno real se enviaría por correo; aquí se devuelve para facilitar pruebas.
        return new PasswordResetResponse("Token de reseteo generado", token);
    }

    /**
     * Resetea la contraseña usando el token previamente emitido.
     */
    @PostMapping("/reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reset(@RequestBody PasswordResetRequest request) {
        passwordResetService.resetPassword(request.token(), request.newPassword());
    }
}
