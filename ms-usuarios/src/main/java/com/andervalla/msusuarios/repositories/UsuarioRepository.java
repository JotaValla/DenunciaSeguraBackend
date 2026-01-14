package com.andervalla.msusuarios.repositories;

import com.andervalla.msusuarios.models.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long> {
    Optional<UsuarioEntity> findByCedula(String cedula);
    Optional<UsuarioEntity> findByEmail(String email);
    Optional<UsuarioEntity> findByPublicCitizenId(String publicCitizenId);
    boolean existsByCedula(String cedula);
    boolean existsByEmail(String email);
    boolean existsByPublicCitizenId(String publicCitizenId);
}
