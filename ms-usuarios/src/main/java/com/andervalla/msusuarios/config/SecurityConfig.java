package com.andervalla.msusuarios.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * URL pública del emisor (claim {@code iss}) que debe coincidir al validar el token.
     */
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    /**
     * URL para obtener el conjunto de llaves JWK utilizadas al validar firmas JWT.
     */
    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    /**
     * Cadena de filtros para rutas internas expuestas sin autenticación.
     */
    @Bean
    @Order(0)
    public SecurityFilterChain internalChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/interno/**")
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .oauth2ResourceServer(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }

    /**
     * Cadena principal que protege el resto de endpoints como recurso OAuth2 con JWT.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health").permitAll()
                .anyRequest().authenticated())
                // Usamos nuestro decodificador personalizado
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt
                        .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        .decoder(jwtDecoder())))
                .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }

    /**
     * Configura un {@link JwtDecoder} con validación de emisor y audiencia.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        // 1. Configura el decodificador apuntando a donde están las llaves públicas
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();

        // 2. Define la validación de la audiencia ("ds-web")
        OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator("ds-web");

        // 3. Define la validación del emisor (debe coincidir con la URL pública)
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuerUri);

        // 4. Combina ambas validaciones
        OAuth2TokenValidator<Jwt> withAudience = new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);

        jwtDecoder.setJwtValidator(withAudience);
        return jwtDecoder;
    }

    /**
     * Ajusta el convertidor para mapear claims a autoridades y añadir el rol personalizado.
     */
    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter baseConverter = new JwtGrantedAuthoritiesConverter();
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            var authorities = new java.util.HashSet<>(baseConverter.convert(jwt));
            String rol = jwt.getClaimAsString("rol");
            if (rol != null && !rol.isBlank()) {
                authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority(
                        "ROLE_" + rol.toUpperCase()));
            }
            return authorities;
        });
        return converter;
    }

    /**
     * Validador de audiencia esperada dentro del token JWT.
     */
    static class AudienceValidator implements OAuth2TokenValidator<Jwt> {
        private final String audience;

        AudienceValidator(String audience) {
            this.audience = audience;
        }

        public OAuth2TokenValidatorResult validate(Jwt jwt) {
            List<String> audiences = jwt.getAudience();
            if (audiences.contains(this.audience)) {
                return OAuth2TokenValidatorResult.success();
            }
            OAuth2Error err = new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN);
            return OAuth2TokenValidatorResult.failure(err);
        }
    }
}
