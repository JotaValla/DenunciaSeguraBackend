package com.andervalla.msauth.services.security;

import com.andervalla.msauth.repositories.CredencialRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Incrementa intentos fallidos y bloquea temporalmente tras múltiples fallos.
 */
@Component
public class AuthenticationFailureListener implements ApplicationListener<AuthenticationFailureBadCredentialsEvent> {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationFailureListener.class);
    private static final int MAX_ATTEMPTS = 3;
    private static final long LOCK_MINUTES = 15;

    private final CredencialRepository credencialRepository;

    public AuthenticationFailureListener(CredencialRepository credencialRepository) {
        this.credencialRepository = credencialRepository;
    }

    @Override
    @Transactional
    public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent event) {
        String cedula = event.getAuthentication().getName();
        credencialRepository.findByCedula(cedula).ifPresent(cred -> {
            int attempts = cred.getFailedAttempts() + 1;
            cred.setFailedAttempts(attempts);
            if (attempts >= MAX_ATTEMPTS) {
                cred.setLockedUntil(Instant.now().plus(LOCK_MINUTES, ChronoUnit.MINUTES));
                log.warn("Usuario {} bloqueado por {} minutos tras {} intentos fallidos", cedula, LOCK_MINUTES, attempts);
            }
            credencialRepository.save(cred);
        });
    }
}
