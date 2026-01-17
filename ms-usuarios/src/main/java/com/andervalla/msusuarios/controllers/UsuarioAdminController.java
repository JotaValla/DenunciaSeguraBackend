package com.andervalla.msusuarios.controllers;

import com.andervalla.msusuarios.controllers.dtos.responses.UsuarioResponse;
import com.andervalla.msusuarios.models.enums.EntidadEnum;
import com.andervalla.msusuarios.services.IUsuarioService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioAdminController {

    private final IUsuarioService usuarioService;

    public UsuarioAdminController(IUsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/jefe")
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN')")
    public UsuarioResponse obtenerJefePorEntidad(@RequestParam EntidadEnum entidad) {
        return usuarioService.obtenerJefePorEntidad(entidad);
    }

    @GetMapping("/operadores")
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN')")
    public List<UsuarioResponse> obtenerOperadoresPorEntidad(@RequestParam EntidadEnum entidad) {
        return usuarioService.obtenerOperadoresPorEntidad(entidad);
    }
}
