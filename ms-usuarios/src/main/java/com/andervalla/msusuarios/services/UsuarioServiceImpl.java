package com.andervalla.msusuarios.services;

import com.andervalla.msusuarios.controllers.dtos.requests.ActualizarAliasRequest;
import com.andervalla.msusuarios.controllers.dtos.requests.RegistroCiudadanoRequest;
import com.andervalla.msusuarios.controllers.dtos.requests.RegistroStaffRequest;
import com.andervalla.msusuarios.controllers.dtos.responses.AliasResponse;
import com.andervalla.msusuarios.controllers.dtos.responses.UsuarioPublicoResponse;
import com.andervalla.msusuarios.controllers.dtos.responses.UsuarioResponse;
import com.andervalla.msusuarios.exceptions.AliasPublicoInvalidoException;
import com.andervalla.msusuarios.exceptions.RolEntidadInvalidaException;
import com.andervalla.msusuarios.exceptions.UsuarioConflictException;
import com.andervalla.msusuarios.exceptions.UsuarioNotFoundException;
import com.andervalla.msusuarios.models.UsuarioEntity;
import com.andervalla.msusuarios.models.enums.EntidadEnum;
import com.andervalla.msusuarios.models.enums.EstadoUsuarioEnum;
import com.andervalla.msusuarios.models.enums.RolEnum;
import com.andervalla.msusuarios.repositories.UsuarioRepository;
import com.andervalla.msusuarios.services.mappers.UsuarioMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Locale;

@Service
public class UsuarioServiceImpl implements IUsuarioService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String PUBLIC_CITIZEN_PREFIX = "cit_";

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, UsuarioMapper usuarioMapper) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
    }

    @Override
    @Transactional
    public UsuarioResponse crearCiudadano(RegistroCiudadanoRequest request) {
        validarUnicidad(request.cedula(), request.email());

        String publicCitizenId = generarPublicCitizenId();

        UsuarioEntity usuario = UsuarioEntity.builder()
                .cedula(request.cedula())
                .email(request.email().toLowerCase(Locale.ROOT))
                .nombre(request.nombre())
                .rol(RolEnum.CIUDADANO)
                .entidad(null)
                .aliasPublico(null)
                .publicCitizenId(publicCitizenId)
                .estado(EstadoUsuarioEnum.ACTIVO)
                .build();

        UsuarioEntity guardado = usuarioRepository.save(usuario);
        return usuarioMapper.toUsuarioResponse(guardado);
    }

    @Override
    @Transactional
    public UsuarioResponse crearStaff(RegistroStaffRequest request) {
        validarUnicidad(request.cedula(), request.email());

        if (request.rol() == RolEnum.CIUDADANO) {
            throw new RolEntidadInvalidaException("Rol CIUDADANO no permitido para staff.");
        }

        validarEntidadRol(request.rol(), request.entidad());
        validarAliasSiAplica(request.aliasPublico(), request);

        UsuarioEntity usuario = UsuarioEntity.builder()
                .cedula(request.cedula())
                .email(request.email().toLowerCase(Locale.ROOT))
                .nombre(request.nombre())
                .rol(request.rol())
                .entidad(request.entidad())
                .aliasPublico(request.aliasPublico())
                .publicCitizenId(null)
                .estado(EstadoUsuarioEnum.ACTIVO)
                .build();

        UsuarioEntity guardado = usuarioRepository.save(usuario);
        return usuarioMapper.toUsuarioResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse obtenerUsuario(Long usuarioId) {
        UsuarioEntity usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioNotFoundException(usuarioId));
        return usuarioMapper.toUsuarioResponse(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorCedula(String cedula) {
        UsuarioEntity usuario = usuarioRepository.findByCedula(cedula)
                .orElseThrow(() -> new UsuarioNotFoundException(cedula));
        return usuarioMapper.toUsuarioResponse(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioPublicoResponse obtenerPublico(Long usuarioId) {
        UsuarioEntity usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioNotFoundException(usuarioId));
        return usuarioMapper.toUsuarioPublicoResponse(usuario);
    }

    @Override
    @Transactional
    public AliasResponse actualizarAlias(Long usuarioId, ActualizarAliasRequest request) {
        UsuarioEntity usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioNotFoundException(usuarioId));

        if (usuario.getRol() == RolEnum.CIUDADANO) {
            throw new RolEntidadInvalidaException("Alias publico no aplica para ciudadanos.");
        }

        validarAliasUsuario(request.aliasPublico(), usuario);
        usuario.setAliasPublico(request.aliasPublico());
        usuarioRepository.save(usuario);
        return new AliasResponse(usuario.getAliasPublico());
    }

    private void validarUnicidad(String cedula, String email) {
        if (usuarioRepository.existsByCedula(cedula)) {
            throw new UsuarioConflictException("Cedula ya registrada.");
        }
        if (usuarioRepository.existsByEmail(email.toLowerCase(Locale.ROOT))) {
            throw new UsuarioConflictException("Email ya registrado.");
        }
    }

    private void validarEntidadRol(RolEnum rol, EntidadEnum entidad) {
        if (entidad == null) {
            throw new RolEntidadInvalidaException("Entidad es obligatoria para staff.");
        }
        if (rol == RolEnum.OP_INT || rol == RolEnum.JEFE_OP_INT) {
            if (entidad != EntidadEnum.MUNICIPIO) {
                throw new RolEntidadInvalidaException("Rol interno requiere entidad MUNICIPIO.");
            }
        }
        if (rol == RolEnum.OP_EXT || rol == RolEnum.JEFE_OP_EXT) {
            if (entidad == EntidadEnum.MUNICIPIO) {
                throw new RolEntidadInvalidaException("Rol externo requiere entidad distinta de MUNICIPIO.");
            }
        }
    }

    private void validarAliasSiAplica(String aliasPublico, RegistroStaffRequest request) {
        if (aliasPublico == null || aliasPublico.isBlank()) {
            return;
        }
        validarAliasContenido(aliasPublico, request.cedula(), request.email());
    }

    private void validarAliasUsuario(String aliasPublico, UsuarioEntity usuario) {
        validarAliasContenido(aliasPublico, usuario.getCedula(), usuario.getEmail());
    }

    private void validarAliasContenido(String aliasPublico, String cedula, String email) {
        if (aliasPublico.contains("@")) {
            throw new AliasPublicoInvalidoException("Alias no debe contener correo.");
        }
        if (cedula != null && !cedula.isBlank() && aliasPublico.contains(cedula)) {
            throw new AliasPublicoInvalidoException("Alias no debe contener cedula.");
        }
        if (email != null && !email.isBlank() && aliasPublico.contains(email)) {
            throw new AliasPublicoInvalidoException("Alias no debe contener email.");
        }
    }

    private String generarPublicCitizenId() {
        String id;
        do {
            id = PUBLIC_CITIZEN_PREFIX + randomAlphaNumeric(10);
        } while (usuarioRepository.existsByPublicCitizenId(id));
        return id;
    }

    private String randomAlphaNumeric(int length) {
        String alphabet = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(alphabet.charAt(SECURE_RANDOM.nextInt(alphabet.length())));
        }
        return builder.toString();
    }

}
