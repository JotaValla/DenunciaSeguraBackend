package com.andervalla.msauth.clients;

import com.andervalla.msauth.clients.dtos.request.RegistroCiudadanoRequest;
import com.andervalla.msauth.clients.dtos.request.RegistroStaffRequest;
import com.andervalla.msauth.clients.dtos.response.UsuarioResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Cliente Feign para invocar los endpoints internos de ms-usuarios.
 */
@FeignClient(name = "ms-usuarios", url = "${CLIENTS_USUARIOS_URI:http://localhost:9091}")
public interface MsUsuariosClient {

    /**
     * Crea un ciudadano en ms-usuarios.
     */
    @PostMapping("/interno/usuarios/ciudadano")
    UsuarioResponse crearCiudadano(@RequestBody RegistroCiudadanoRequest request);

    /**
     * Crea un usuario de staff en ms-usuarios.
     */
    @PostMapping("/interno/usuarios/staff")
    UsuarioResponse crearStaff(@RequestBody RegistroStaffRequest request);

    /**
     * Obtiene un usuario por cédula desde ms-usuarios.
     */
    @GetMapping("/interno/usuarios/cedula/{cedula}")
    UsuarioResponse obtenerPorCedula(@PathVariable String cedula);
}
