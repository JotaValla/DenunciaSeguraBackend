package com.andervalla.msusuarios.controllers;

import com.andervalla.msusuarios.controllers.dtos.responses.UsuarioResponse;
import com.andervalla.msusuarios.services.IUsuarioService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoint para leer el perfil del usuario autenticado. */
@RestController
@RequestMapping({"/usuarios", ""})
public class UsuarioPerfilController {

    private final IUsuarioService usuarioService;

    public UsuarioPerfilController(IUsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    /**
     * Resuelve el usuario desde `usuario_id` (claim) o, si no existe, desde `sub`.
     */
    public UsuarioResponse obtenerPerfil(@AuthenticationPrincipal Jwt jwt) {
        Long usuarioId = jwt.getClaim("usuario_id");
        if (usuarioId == null) {
            usuarioId = Long.valueOf(jwt.getSubject());
        }
        return usuarioService.obtenerUsuario(usuarioId);
    }
}
