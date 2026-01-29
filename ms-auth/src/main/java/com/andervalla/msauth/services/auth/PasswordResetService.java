package com.andervalla.msauth.services.auth;

import com.andervalla.msauth.clients.MsUsuariosClient;
import com.andervalla.msauth.clients.dtos.response.UsuarioResponse;
import com.andervalla.msauth.exceptions.CredentialNotFoundException;
import com.andervalla.msauth.exceptions.PasswordResetTokenExpiredException;
import com.andervalla.msauth.exceptions.PasswordResetTokenInvalidException;
import com.andervalla.msauth.exceptions.TokenHashingException;
import com.andervalla.msauth.models.CredencialEntity;
import com.andervalla.msauth.models.PasswordResetTokenEntity;
import com.andervalla.msauth.repositories.CredencialRepository;
import com.andervalla.msauth.repositories.PasswordResetTokenRepository;
import com.andervalla.msauth.services.notification.MailService;
import com.andervalla.msauth.services.security.PasswordPolicyValidator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;

/**
 * Gestiona la generación y uso de tokens de reseteo de contraseñas.
 */
@Service
public class PasswordResetService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private final PasswordResetTokenRepository tokenRepository;
    private final CredencialRepository credencialRepository;
    private final MsUsuariosClient msUsuariosClient;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final PasswordPolicyValidator passwordPolicyValidator;

    /**
     * Constructor con dependencias necesarias.
     */
    public PasswordResetService(PasswordResetTokenRepository tokenRepository,
                                CredencialRepository credencialRepository,
                                MsUsuariosClient msUsuariosClient,
                                PasswordEncoder passwordEncoder,
                                MailService mailService,
                                PasswordPolicyValidator passwordPolicyValidator) {
        this.tokenRepository = tokenRepository;
        this.credencialRepository = credencialRepository;
        this.msUsuariosClient = msUsuariosClient;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
        this.passwordPolicyValidator = passwordPolicyValidator;
    }

    /**
     * Genera y persiste un token de reseteo para la cédula indicada.
     */
    @Transactional
    public String generateResetToken(String cedula) {
        UsuarioResponse usuario = msUsuariosClient.obtenerPorCedula(cedula);
        CredencialEntity credencial = credencialRepository.findByUsuarioId(usuario.id())
                .orElseThrow(() -> new CredentialNotFoundException("Credenciales no encontradas para usuario"));

        String rawToken = randomToken();
        String hashedToken = hashToken(rawToken);
        PasswordResetTokenEntity entity = PasswordResetTokenEntity.builder()
                .usuarioId(credencial.getUsuarioId())
                .tokenHash(hashedToken)
                .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS)) // Token válido por 1 hora
                .build();
        tokenRepository.save(entity);
        mailService.enviarResetPassword(usuario.email(), rawToken);
        return rawToken;
    }

    /**
     * Valida el token y actualiza la contraseña del usuario.
     */
    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        passwordPolicyValidator.validate(newPassword);
        PasswordResetTokenEntity token = tokenRepository.findByTokenHash(hashToken(rawToken))
                .orElseThrow(() -> new PasswordResetTokenInvalidException("Token invalido"));
        if (token.getUsedAt() != null || token.getExpiresAt().isBefore(Instant.now())) {
            throw new PasswordResetTokenExpiredException("Token expirado o ya usado");
        }

        CredencialEntity credencial = credencialRepository.findByUsuarioId(token.getUsuarioId())
                .orElseThrow(() -> new CredentialNotFoundException("Credenciales no encontradas"));

        credencial.setPasswordHash(passwordEncoder.encode(newPassword));
        credencial.setLockedUntil(null);
        credencial.setFailedAttempts(0);
        credencialRepository.save(credencial);

        token.setUsedAt(Instant.now());
        tokenRepository.save(token);
    }

    /**
     * Genera un token aleatorio en formato hex.
     */
    private String randomToken() {
        byte[] bytes = new byte[24];
        RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawToken.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new TokenHashingException("No se pudo hashear el token", e);
        }
    }
}
