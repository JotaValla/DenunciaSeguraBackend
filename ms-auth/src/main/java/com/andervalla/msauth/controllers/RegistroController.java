package com.andervalla.msauth.controllers;

import com.andervalla.msauth.controllers.dtos.request.RegistroCiudadanoAuthRequest;
import com.andervalla.msauth.controllers.dtos.request.RegistroStaffAuthRequest;
import com.andervalla.msauth.controllers.dtos.response.RegistroUsuarioResponse;
import com.andervalla.msauth.services.auth.UsuarioProvisioningService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints para registrar usuarios (ciudadanos y staff) vía OAuth server.
 */
@RestController
@RequestMapping("/register")
public class RegistroController {

    private final UsuarioProvisioningService usuarioProvisioningService;

    /**
     * Constructor con inyección de dependencias.
     */
    public RegistroController(UsuarioProvisioningService usuarioProvisioningService) {
        this.usuarioProvisioningService = usuarioProvisioningService;
    }

    /**
     * Registra un ciudadano y devuelve información de creación.
     */
    @PostMapping("/citizen")
    @ResponseStatus(HttpStatus.CREATED)
    public RegistroUsuarioResponse registrarCiudadano(@Valid @RequestBody RegistroCiudadanoAuthRequest request) {
        return usuarioProvisioningService.registrarCiudadano(request);
    }

    /**
     * Registra un miembro de staff y devuelve información de creación.
     */
    @PostMapping("/staff")
    @ResponseStatus(HttpStatus.CREATED)
    public RegistroUsuarioResponse registrarStaff(@Valid @RequestBody RegistroStaffAuthRequest request) {
        return usuarioProvisioningService.registrarStaff(request);
    }
}
