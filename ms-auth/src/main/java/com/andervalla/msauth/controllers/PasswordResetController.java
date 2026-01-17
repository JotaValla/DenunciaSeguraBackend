package com.andervalla.msauth.controllers;

import com.andervalla.msauth.controllers.dtos.PasswordForgotRequest;
import com.andervalla.msauth.controllers.dtos.PasswordResetRequest;
import com.andervalla.msauth.controllers.dtos.PasswordResetResponse;
import com.andervalla.msauth.services.PasswordResetService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/password")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/forgot")
    @ResponseStatus(HttpStatus.CREATED)
    public PasswordResetResponse forgot(@RequestBody PasswordForgotRequest request) {
        String token = passwordResetService.generateResetToken(request.cedula());
        // En un entorno real se enviaría por correo; aquí se devuelve para facilitar pruebas.
        return new PasswordResetResponse("Token de reseteo generado", token);
    }

    @PostMapping("/reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reset(@RequestBody PasswordResetRequest request) {
        passwordResetService.resetPassword(request.token(), request.newPassword());
    }
}
