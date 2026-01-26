package com.andervalla.msauth.services.auth;

import com.andervalla.msauth.clients.MsUsuariosClient;
import com.andervalla.msauth.clients.dtos.request.RegistroCiudadanoRequest;
import com.andervalla.msauth.clients.dtos.request.RegistroStaffRequest;
import com.andervalla.msauth.clients.dtos.response.UsuarioResponse;
import com.andervalla.msauth.controllers.dtos.request.RegistroCiudadanoAuthRequest;
import com.andervalla.msauth.controllers.dtos.request.RegistroStaffAuthRequest;
import com.andervalla.msauth.controllers.dtos.response.RegistroUsuarioResponse;
import com.andervalla.msauth.models.CredencialEntity;
import com.andervalla.msauth.repositories.CredencialRepository;
import com.andervalla.msauth.services.security.PasswordPolicyValidator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Orquesta el alta de usuarios en ms-usuarios y la creación de credenciales locales.
 */
@Service
public class UsuarioProvisioningService {

    private final MsUsuariosClient msUsuariosClient;
    private final CredencialRepository credencialRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicyValidator passwordPolicyValidator;

    /**
     * Constructor con dependencias requeridas.
     */
    public UsuarioProvisioningService(MsUsuariosClient msUsuariosClient,
                                      CredencialRepository credencialRepository,
                                      PasswordEncoder passwordEncoder,
                                      PasswordPolicyValidator passwordPolicyValidator) {
        this.msUsuariosClient = msUsuariosClient;
        this.credencialRepository = credencialRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordPolicyValidator = passwordPolicyValidator;
    }

    /**
     * Registra un ciudadano en ms-usuarios y genera sus credenciales.
     */
    @Transactional
    public RegistroUsuarioResponse registrarCiudadano(RegistroCiudadanoAuthRequest request) {
        passwordPolicyValidator.validate(request.password());
        UsuarioResponse usuario = msUsuariosClient.crearCiudadano(new RegistroCiudadanoRequest(
                request.email(),
                request.nombre(),
                request.cedula()
        ));

        crearCredencial(usuario, request.password());
        return new RegistroUsuarioResponse(usuario.id(), usuario.publicCitizenId(), "Usuario creado exitosamente.");
    }

    /**
     * Registra un usuario de staff en ms-usuarios y crea credenciales locales.
     */
    @Transactional
    public RegistroUsuarioResponse registrarStaff(RegistroStaffAuthRequest request) {
        UsuarioResponse usuario = msUsuariosClient.crearStaff(new RegistroStaffRequest(
                request.email(),
                request.nombre(),
                request.cedula(),
                request.rol(),
                request.entidad()
        ));

        String password = Optional.ofNullable(request.password()).filter(p -> !p.isBlank()).orElse("Temp#12345");
        passwordPolicyValidator.validate(password);
        crearCredencial(usuario, password);
        return new RegistroUsuarioResponse(usuario.id(), usuario.publicCitizenId(), "Usuario creado exitosamente.");
    }

    /**
     * Recupera información de usuario por cédula desde ms-usuarios.
     */
    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorCedula(String cedula) {
        return msUsuariosClient.obtenerPorCedula(cedula);
    }

    /**
     * Crea y persiste la credencial local para el usuario recién creado.
     */
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
