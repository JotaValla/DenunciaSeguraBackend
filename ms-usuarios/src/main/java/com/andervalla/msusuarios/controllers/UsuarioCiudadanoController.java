package com.andervalla.msusuarios.controllers;

import com.andervalla.msusuarios.controllers.dtos.requests.ActualizarAliasRequest;
import com.andervalla.msusuarios.controllers.dtos.responses.AliasResponse;
import com.andervalla.msusuarios.services.IUsuarioService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/usuarios")
public class UsuarioCiudadanoController {

    private final IUsuarioService usuarioService;

    public UsuarioCiudadanoController(IUsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PatchMapping("/alias")
    @PreAuthorize("hasRole('CIUDADANO')")
    public AliasResponse actualizarAliasCiudadano(@Valid @RequestBody ActualizarAliasRequest request,
                                                  JwtAuthenticationToken authentication) {
        Long usuarioId = Long.valueOf(authentication.getToken().getSubject());
        return usuarioService.actualizarAlias(usuarioId, request);
    }
}
