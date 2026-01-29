package com.andervalla.msauth.controllers;

import com.andervalla.msauth.controllers.dtos.request.LogoutRequest;
import com.andervalla.msauth.controllers.dtos.response.LogoutResponse;
import com.andervalla.msauth.services.auth.LogoutService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint para cerrar sesión y revocar refresh tokens del Authorization Server.
 */
@RestController
@RequestMapping("/auth")
public class LogoutController {

    private final LogoutService logoutService;

    public LogoutController(LogoutService logoutService) {
        this.logoutService = logoutService;
    }

    /**
     * Revoca el refresh token (si se envía) e invalida la sesión HTTP actual.
     */
    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout(@RequestBody LogoutRequest request,
                                                 HttpServletRequest httpRequest,
                                                 HttpServletResponse httpResponse) {
        String refreshToken = request != null ? request.refreshToken() : null;
        boolean refreshRevocado = logoutService.revocarRefreshToken(refreshToken);
        boolean sesionInvalidada = logoutService.invalidarSesion(httpRequest, httpResponse);

        String mensaje;
        if (refreshRevocado && sesionInvalidada) {
            mensaje = "Refresh token revocado y sesión invalidada.";
        } else if (refreshRevocado) {
            mensaje = "Refresh token revocado.";
        } else if (sesionInvalidada) {
            mensaje = "Sesión invalidada.";
        } else {
            mensaje = "No se encontró refresh token ni sesión activa.";
        }

        return ResponseEntity.ok(new LogoutResponse(mensaje, refreshRevocado, sesionInvalidada));
    }
}
