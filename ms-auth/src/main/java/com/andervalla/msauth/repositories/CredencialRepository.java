package com.andervalla.msauth.repositories;

import com.andervalla.msauth.models.CredencialEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CredencialRepository extends JpaRepository<CredencialEntity, Long> {
    Optional<CredencialEntity> findByCedula(String cedula);
    Optional<CredencialEntity> findByUsuarioId(Long usuarioId);
}
