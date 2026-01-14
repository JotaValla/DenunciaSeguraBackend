package com.andervalla.msauth.services;

import com.andervalla.msauth.clients.MsUsuariosClient;
import com.andervalla.msauth.clients.dtos.RegistroCiudadanoRequest;
import com.andervalla.msauth.clients.dtos.RegistroStaffRequest;
import com.andervalla.msauth.clients.dtos.UsuarioResponse;
import com.andervalla.msauth.controllers.dtos.RegistroCiudadanoAuthRequest;
import com.andervalla.msauth.controllers.dtos.RegistroStaffAuthRequest;
import com.andervalla.msauth.controllers.dtos.RegistroUsuarioResponse;
import com.andervalla.msauth.models.CredencialEntity;
import com.andervalla.msauth.repositories.CredencialRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UsuarioProvisioningService {

    private final MsUsuariosClient msUsuariosClient;
    private final CredencialRepository credencialRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioProvisioningService(MsUsuariosClient msUsuariosClient,
                                      CredencialRepository credencialRepository,
                                      PasswordEncoder passwordEncoder) {
        this.msUsuariosClient = msUsuariosClient;
        this.credencialRepository = credencialRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RegistroUsuarioResponse registrarCiudadano(RegistroCiudadanoAuthRequest request) {
        UsuarioResponse usuario = msUsuariosClient.crearCiudadano(new RegistroCiudadanoRequest(
                request.email(),
                request.nombre(),
                request.cedula()
        ));

        crearCredencial(usuario, request.password());
        return new RegistroUsuarioResponse(usuario.id(), usuario.publicCitizenId(), "Usuario creado exitosamente.");
    }

    @Transactional
    public RegistroUsuarioResponse registrarStaff(RegistroStaffAuthRequest request) {
        UsuarioResponse usuario = msUsuariosClient.crearStaff(new RegistroStaffRequest(
                request.email(),
                request.nombre(),
                request.cedula(),
                request.rol(),
                request.entidad(),
                request.aliasPublico()
        ));

        String password = Optional.ofNullable(request.password()).filter(p -> !p.isBlank()).orElse("Temp#12345");
        crearCredencial(usuario, password);
        return new RegistroUsuarioResponse(usuario.id(), usuario.publicCitizenId(), "Usuario creado exitosamente.");
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorCedula(String cedula) {
        return msUsuariosClient.obtenerPorCedula(cedula);
    }

    private void crearCredencial(UsuarioResponse usuario, String rawPassword) {
        CredencialEntity credencial = CredencialEntity.builder()
                .usuarioId(usuario.id())
                .cedula(usuario.cedula())
                .passwordHash(passwordEncoder.encode(rawPassword))
                .enabled(true)
                .build();

        credencialRepository.save(credencial);
    }

}
