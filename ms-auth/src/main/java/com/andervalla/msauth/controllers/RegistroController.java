package com.andervalla.msauth.controllers;

import com.andervalla.msauth.controllers.dtos.RegistroCiudadanoAuthRequest;
import com.andervalla.msauth.controllers.dtos.RegistroStaffAuthRequest;
import com.andervalla.msauth.controllers.dtos.RegistroUsuarioResponse;
import com.andervalla.msauth.services.UsuarioProvisioningService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/register")
public class RegistroController {

    private final UsuarioProvisioningService usuarioProvisioningService;

    public RegistroController(UsuarioProvisioningService usuarioProvisioningService) {
        this.usuarioProvisioningService = usuarioProvisioningService;
    }

    @PostMapping("/citizen")
    @ResponseStatus(HttpStatus.CREATED)
    public RegistroUsuarioResponse registrarCiudadano(@Valid @RequestBody RegistroCiudadanoAuthRequest request) {
        return usuarioProvisioningService.registrarCiudadano(request);
    }

    @PostMapping("/staff")
    @ResponseStatus(HttpStatus.CREATED)
    public RegistroUsuarioResponse registrarStaff(@Valid @RequestBody RegistroStaffAuthRequest request) {
        return usuarioProvisioningService.registrarStaff(request);
    }
}
