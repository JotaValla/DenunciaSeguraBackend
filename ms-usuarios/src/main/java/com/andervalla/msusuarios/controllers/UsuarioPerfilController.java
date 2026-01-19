package com.andervalla.msusuarios.controllers;

import com.andervalla.msusuarios.controllers.dtos.responses.UsuarioResponse;
import com.andervalla.msusuarios.services.IUsuarioService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/usuarios", ""})
public class UsuarioPerfilController {

    private final IUsuarioService usuarioService;

    public UsuarioPerfilController(IUsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public UsuarioResponse obtenerPerfil(@AuthenticationPrincipal Jwt jwt) {
        Long usuarioId = jwt.getClaim("usuario_id");
        if (usuarioId == null) {
            usuarioId = Long.valueOf(jwt.getSubject());
        }
        return usuarioService.obtenerUsuario(usuarioId);
    }
}
