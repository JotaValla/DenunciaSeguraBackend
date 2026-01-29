package com.andervalla.msauth.repositories;

import com.andervalla.msauth.models.PasswordResetTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio para tokens de reseteo de contraseña.
 */
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, Long> {
    /**
     * Obtiene un token por su hash almacenado.
     */
    Optional<PasswordResetTokenEntity> findByTokenHash(String tokenHash);
}
