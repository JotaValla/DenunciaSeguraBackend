package com.andervalla.msauth.services.security;

import com.andervalla.msauth.repositories.CredencialRepository;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Resetea intentos y registra último login tras autenticación exitosa.
 */
@Component
public class AuthenticationSuccessListener implements ApplicationListener<AuthenticationSuccessEvent> {

    private final CredencialRepository credencialRepository;

    public AuthenticationSuccessListener(CredencialRepository credencialRepository) {
        this.credencialRepository = credencialRepository;
    }

    @Override
    @Transactional
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        String cedula = event.getAuthentication().getName();
        credencialRepository.findByCedula(cedula).ifPresent(cred -> {
            cred.setFailedAttempts(0);
            cred.setLockedUntil(null);
            cred.setLastLoginAt(Instant.now());
            credencialRepository.save(cred);
        });
    }
}
