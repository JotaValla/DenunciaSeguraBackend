package com.andervalla.msauth.services.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Envía correos simples; en dev registra advertencias si no hay configuración SMTP.
 */
@Slf4j
@Service
public class MailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.security.reset-url:http://localhost:5173/reset-password?token=}")
    private String resetUrl;

    /**
     * Envía (o registra) el correo con el enlace de reseteo de contraseña.
     */
    public void enviarResetPassword(String emailDestino, String token) {
        if (mailSender == null) {
            log.warn("MailSender no configurado; token de reset para {}: {}", emailDestino, token);
            return;
        }
        String link = resetUrl + token;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(emailDestino);
        message.setSubject("Recuperación de contraseña");
        message.setText("Usa el siguiente enlace para restablecer tu contraseña:\n" + link);
        try {
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Error enviando email de reset a {}", emailDestino, e);
        }
    }
}
