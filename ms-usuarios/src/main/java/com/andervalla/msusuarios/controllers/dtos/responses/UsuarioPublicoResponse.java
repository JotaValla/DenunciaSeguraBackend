package com.andervalla.msusuarios.controllers.dtos.responses;

import com.andervalla.msusuarios.models.enums.EntidadEnum;
import com.andervalla.msusuarios.models.enums.RolEnum;

public record UsuarioPublicoResponse(
        Long id,
        RolEnum rol,
        EntidadEnum entidad,
        String aliasPublico
) {
}
