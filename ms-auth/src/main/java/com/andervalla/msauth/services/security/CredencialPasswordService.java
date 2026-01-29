package com.andervalla.msauth.services.security;

import com.andervalla.msauth.models.CredencialEntity;
import com.andervalla.msauth.repositories.CredencialRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Actualiza el hash de contraseña cuando el encoder lo requiere (rehash).
 */
@Service
public class CredencialPasswordService implements UserDetailsPasswordService {

    private final CredencialRepository credencialRepository;

    public CredencialPasswordService(CredencialRepository credencialRepository) {
        this.credencialRepository = credencialRepository;
    }

    @Override
    @Transactional
    public UserDetails updatePassword(UserDetails user, String newPassword) {
        credencialRepository.findByCedula(user.getUsername()).ifPresent(cred -> {
            cred.setPasswordHash(newPassword);
            credencialRepository.save(cred);
        });
        // Devuelve un User con el hash actualizado para seguir el flujo de Spring Security.
        return User.withUserDetails(user).password(newPassword).build();
    }
}
