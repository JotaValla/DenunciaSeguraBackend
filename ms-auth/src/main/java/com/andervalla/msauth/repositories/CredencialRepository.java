package com.andervalla.msauth.repositories;

import com.andervalla.msauth.models.CredencialEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Acceso a datos para credenciales locales del Authorization Server.
 */
public interface CredencialRepository extends JpaRepository<CredencialEntity, Long> {
    /**
     * Busca credenciales por número de cédula.
     */
    Optional<CredencialEntity> findByCedula(String cedula);

    /**
     * Busca credenciales por id de usuario remoto.
     */
    Optional<CredencialEntity> findByUsuarioId(Long usuarioId);
}
