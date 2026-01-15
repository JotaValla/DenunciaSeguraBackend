package com.andervalla.msauth.services;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

@Component
public class ServiceTokenProvider {

    private final JwtEncoder jwtEncoder;
    private final String issuer;

    public ServiceTokenProvider(JwtEncoder jwtEncoder,
                                @Value("${app.security.issuer:http://localhost:9092}") String issuer) {
        this.jwtEncoder = jwtEncoder;
        this.issuer = issuer;
    }

    /**
     * Emite un token técnico corto para llamadas internas (rol SERVICE).
     */
    public String issueServiceToken() {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(300)) // 5 minutos
                .subject("ms-auth-service")
                .claim("rol", "SERVICE")
                .audience(java.util.List.of("ms-usuarios"))
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
