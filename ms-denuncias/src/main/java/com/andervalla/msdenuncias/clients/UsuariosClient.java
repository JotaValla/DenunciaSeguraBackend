package com.andervalla.msdenuncias.clients;

import com.andervalla.msdenuncias.clients.dtos.UsuarioDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "ms-usuarios", url = "${CLIENTS_USUARIOS_URI:http://localhost:9091}")
public interface UsuariosClient {

    @GetMapping("/interno/usuarios/jefe")
    UsuarioDTO obtenerJefePorEntidad(@RequestParam("entidad") String entidad);

    @GetMapping("/interno/usuarios/{id}")
    UsuarioDTO obtenerUsuarioPorId(@PathVariable("id") Long id);
}
