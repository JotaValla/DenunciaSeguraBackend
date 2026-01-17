package com.andervalla.msauth.services;

import com.andervalla.msauth.clients.MsUsuariosClient;
import com.andervalla.msauth.clients.dtos.UsuarioResponse;
import com.andervalla.msauth.models.CredencialEntity;
import com.andervalla.msauth.models.PasswordResetTokenEntity;
import com.andervalla.msauth.repositories.CredencialRepository;
import com.andervalla.msauth.repositories.PasswordResetTokenRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;

@Service
public class PasswordResetService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private final PasswordResetTokenRepository tokenRepository;
    private final CredencialRepository credencialRepository;
    private final MsUsuariosClient msUsuariosClient;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    public PasswordResetService(PasswordResetTokenRepository tokenRepository,
                                CredencialRepository credencialRepository,
                                MsUsuariosClient msUsuariosClient,
                                PasswordEncoder passwordEncoder,
                                MailService mailService) {
        this.tokenRepository = tokenRepository;
        this.credencialRepository = credencialRepository;
        this.msUsuariosClient = msUsuariosClient;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
    }

    @Transactional
    public String generateResetToken(String cedula) {
        UsuarioResponse usuario = msUsuariosClient.obtenerPorCedula(cedula);
        CredencialEntity credencial = credencialRepository.findByUsuarioId(usuario.id())
                .orElseThrow(() -> new IllegalStateException("Credenciales no encontradas para usuario"));

        String rawToken = randomToken();
        PasswordResetTokenEntity entity = PasswordResetTokenEntity.builder()
                .usuarioId(credencial.getUsuarioId())
                .tokenHash(rawToken)
                .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                .build();
        tokenRepository.save(entity);
        mailService.enviarResetPassword(usuario.email(), rawToken);
        return rawToken;
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        PasswordResetTokenEntity token = tokenRepository.findByTokenHash(rawToken)
                .orElseThrow(() -> new IllegalStateException("Token invalido"));
        if (token.getUsedAt() != null || token.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalStateException("Token expirado o ya usado");
        }

        CredencialEntity credencial = credencialRepository.findByUsuarioId(token.getUsuarioId())
                .orElseThrow(() -> new IllegalStateException("Credenciales no encontradas"));

        credencial.setPasswordHash(passwordEncoder.encode(newPassword));
        credencial.setLockedUntil(null);
        credencial.setFailedAttempts(0);
        credencialRepository.save(credencial);

        token.setUsedAt(Instant.now());
        tokenRepository.save(token);
    }

    private String randomToken() {
        byte[] bytes = new byte[24];
        RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
