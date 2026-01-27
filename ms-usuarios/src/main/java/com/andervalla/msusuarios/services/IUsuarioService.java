package com.andervalla.msusuarios.services;

import com.andervalla.msusuarios.controllers.dtos.requests.ActualizarAliasRequest;
import com.andervalla.msusuarios.controllers.dtos.requests.RegistroCiudadanoRequest;
import com.andervalla.msusuarios.controllers.dtos.requests.RegistroStaffRequest;
import com.andervalla.msusuarios.controllers.dtos.responses.AliasResponse;
import com.andervalla.msusuarios.controllers.dtos.responses.UsuarioPublicoResponse;
import com.andervalla.msusuarios.controllers.dtos.responses.UsuarioResponse;

/**
 * Contrato de negocio para gestión de usuarios (ciudadanos y staff).
 */
public interface IUsuarioService {
    /** Crea un ciudadano (rol CIUDADANO) validando unicidad por cédula/email. */
    UsuarioResponse crearCiudadano(RegistroCiudadanoRequest request);

    /** Crea un usuario staff (no CIUDADANO) validando rol/entidad. */
    UsuarioResponse crearStaff(RegistroStaffRequest request);

    /** Obtiene un usuario por id interno. */
    UsuarioResponse obtenerUsuario(Long usuarioId);

    /** Obtiene un usuario por cédula. */
    UsuarioResponse obtenerPorCedula(String cedula);

    /** Obtiene vista pública (sin PII) del usuario. */
    UsuarioPublicoResponse obtenerPublico(Long usuarioId);

    /** Actualiza el alias público (solo ciudadanos). */
    AliasResponse actualizarAlias(Long usuarioId, ActualizarAliasRequest request);

    /** Obtiene el jefe de operadores según entidad. */
    UsuarioResponse obtenerJefePorEntidad(com.andervalla.msusuarios.models.enums.EntidadEnum entidad);

    /** Lista operadores según entidad. */
    java.util.List<UsuarioResponse> obtenerOperadoresPorEntidad(com.andervalla.msusuarios.models.enums.EntidadEnum entidad);
}
