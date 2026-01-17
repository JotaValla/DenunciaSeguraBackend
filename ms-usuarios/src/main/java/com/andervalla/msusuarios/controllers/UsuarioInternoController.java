package com.andervalla.msusuarios.controllers;

import com.andervalla.msusuarios.controllers.dtos.requests.ActualizarAliasRequest;
import com.andervalla.msusuarios.controllers.dtos.requests.RegistroCiudadanoRequest;
import com.andervalla.msusuarios.controllers.dtos.requests.RegistroStaffRequest;
import com.andervalla.msusuarios.controllers.dtos.responses.AliasResponse;
import com.andervalla.msusuarios.controllers.dtos.responses.UsuarioPublicoResponse;
import com.andervalla.msusuarios.controllers.dtos.responses.UsuarioResponse;
import com.andervalla.msusuarios.models.enums.EntidadEnum;
import com.andervalla.msusuarios.services.IUsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/interno/usuarios")
public class UsuarioInternoController {

    private final IUsuarioService usuarioService;

    public UsuarioInternoController(IUsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/ciudadano")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse crearCiudadano(@Valid @RequestBody RegistroCiudadanoRequest request) {
        return usuarioService.crearCiudadano(request);
    }

    @PostMapping("/staff")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse crearStaff(@Valid @RequestBody RegistroStaffRequest request) {
        return usuarioService.crearStaff(request);
    }

    @GetMapping("/{id}")
    public UsuarioResponse obtenerUsuario(@PathVariable Long id) {
        return usuarioService.obtenerUsuario(id);
    }

    @GetMapping("/cedula/{cedula}")
    public UsuarioResponse obtenerPorCedula(@PathVariable String cedula) {
        return usuarioService.obtenerPorCedula(cedula);
    }

    @GetMapping("/{id}/publico")
    public UsuarioPublicoResponse obtenerPublico(@PathVariable Long id) {
        return usuarioService.obtenerPublico(id);
    }

    @PatchMapping("/{id}/alias")
    public AliasResponse actualizarAlias(@PathVariable Long id,
                                         @Valid @RequestBody ActualizarAliasRequest request) {
        return usuarioService.actualizarAlias(id, request);
    }

    @GetMapping("/jefe")
    public UsuarioResponse obtenerJefePorEntidad(@RequestParam EntidadEnum entidad) {
        return usuarioService.obtenerJefePorEntidad(entidad);
    }

    @GetMapping("/operadores")
    public java.util.List<UsuarioResponse> obtenerOperadoresPorEntidad(@RequestParam EntidadEnum entidad) {
        return usuarioService.obtenerOperadoresPorEntidad(entidad);
    }
}
