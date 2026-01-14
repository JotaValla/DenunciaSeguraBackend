package com.andervalla.msauth.clients;

import com.andervalla.msauth.clients.dtos.RegistroCiudadanoRequest;
import com.andervalla.msauth.clients.dtos.RegistroStaffRequest;
import com.andervalla.msauth.clients.dtos.UsuarioResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ms-usuarios", url = "${ms-usuarios.url:http://localhost:9090}")
public interface MsUsuariosClient {

    @PostMapping("/interno/usuarios/ciudadano")
    UsuarioResponse crearCiudadano(@RequestBody RegistroCiudadanoRequest request);

    @PostMapping("/interno/usuarios/staff")
    UsuarioResponse crearStaff(@RequestBody RegistroStaffRequest request);

    @GetMapping("/interno/usuarios/cedula/{cedula}")
    UsuarioResponse obtenerPorCedula(@PathVariable String cedula);
}
