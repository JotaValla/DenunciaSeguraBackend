package com.andervalla.msusuarios.services.mappers;

import com.andervalla.msusuarios.controllers.dtos.responses.UsuarioPublicoResponse;
import com.andervalla.msusuarios.controllers.dtos.responses.UsuarioResponse;
import com.andervalla.msusuarios.models.UsuarioEntity;
import org.springframework.stereotype.Component;

@Component
/** Mapper simple: entidad -> DTOs de respuesta. */
public class UsuarioMapper {

    public UsuarioResponse toUsuarioResponse(UsuarioEntity usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getCedula(),
                usuario.getEmail(),
                usuario.getNombre(),
                usuario.getRol(),
                usuario.getEntidad(),
                usuario.getAliasPublico(),
                usuario.getEstado()
        );
    }

    public UsuarioPublicoResponse toUsuarioPublicoResponse(UsuarioEntity usuario) {
        return new UsuarioPublicoResponse(
                usuario.getId(),
                usuario.getRol(),
                usuario.getEntidad(),
                usuario.getAliasPublico()
        );
    }
}
