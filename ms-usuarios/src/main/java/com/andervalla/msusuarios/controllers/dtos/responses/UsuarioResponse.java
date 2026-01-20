package com.andervalla.msusuarios.controllers.dtos.responses;

import com.andervalla.msusuarios.models.enums.EntidadEnum;
import com.andervalla.msusuarios.models.enums.EstadoUsuarioEnum;
import com.andervalla.msusuarios.models.enums.RolEnum;

public record UsuarioResponse(
        Long id,
        String cedula,
        String email,
        String nombre,
        RolEnum rol,
        EntidadEnum entidad,
        String aliasPublico,
        EstadoUsuarioEnum estado
) {
}
