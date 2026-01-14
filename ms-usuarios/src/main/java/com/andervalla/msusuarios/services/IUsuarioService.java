package com.andervalla.msusuarios.services;

import com.andervalla.msusuarios.controllers.dtos.requests.ActualizarAliasRequest;
import com.andervalla.msusuarios.controllers.dtos.requests.RegistroCiudadanoRequest;
import com.andervalla.msusuarios.controllers.dtos.requests.RegistroStaffRequest;
import com.andervalla.msusuarios.controllers.dtos.responses.AliasResponse;
import com.andervalla.msusuarios.controllers.dtos.responses.UsuarioPublicoResponse;
import com.andervalla.msusuarios.controllers.dtos.responses.UsuarioResponse;

public interface IUsuarioService {
    UsuarioResponse crearCiudadano(RegistroCiudadanoRequest request);
    UsuarioResponse crearStaff(RegistroStaffRequest request);
    UsuarioResponse obtenerUsuario(Long usuarioId);
    UsuarioResponse obtenerPorCedula(String cedula);
    UsuarioPublicoResponse obtenerPublico(Long usuarioId);
    AliasResponse actualizarAlias(Long usuarioId, ActualizarAliasRequest request);
}
