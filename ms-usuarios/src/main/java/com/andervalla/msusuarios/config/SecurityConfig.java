// package com.andervalla.msusuarios.config;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.core.annotation.Order;
// import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
// import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
// import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
// import org.springframework.security.web.SecurityFilterChain;

// @Configuration
// @EnableMethodSecurity
// public class SecurityConfig {

//     @Bean
//     @Order(0)
//     public SecurityFilterChain internalChain(HttpSecurity http) throws Exception {
//         http.securityMatcher("/interno/**")
//                 .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
//                 .oauth2ResourceServer(AbstractHttpConfigurer::disable)
//                 .csrf(AbstractHttpConfigurer::disable);
//         return http.build();
//     }

//     @Bean
//     @Order(1)
//     public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//         http.authorizeHttpRequests(auth -> auth
//                         .requestMatchers("/actuator/health").permitAll()
//                         .anyRequest().authenticated()
//                 )
//                 .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
//                 .csrf(AbstractHttpConfigurer::disable);
//         return http.build();
//     }

//     private JwtAuthenticationConverter jwtAuthenticationConverter() {
//         JwtGrantedAuthoritiesConverter baseConverter = new JwtGrantedAuthoritiesConverter();
//         JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
//         converter.setJwtGrantedAuthoritiesConverter(jwt -> {
//             var authorities = new java.util.HashSet<>(baseConverter.convert(jwt));
//             String rol = jwt.getClaimAsString("rol");
//             if (rol != null && !rol.isBlank()) {
//                 authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + rol.toUpperCase()));
//             }
//             return authorities;
//         });
//         return converter;
//     }
// }

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

    // Inyectamos la URL pública (la que aparece en el token "iss")
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    // Inyectamos la URL interna o directa para descargar las llaves (JWK)
    // A veces es la misma que el issuer, pero en contenedores a veces es mejor usar
    // la interna
    // Ej: http://gateway:8081/auth/.well-known/jwks.json o la pública si hay salida
    // a internet.
    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Bean
    @Order(0)
    public SecurityFilterChain internalChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/interno/**")
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .oauth2ResourceServer(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }

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

    // --- AQUÍ ESTÁ LA MAGIA PARA VALIDAR AUDIENCIA Y EMISOR ---
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

    // --- CLASE INTERNA PARA VALIDAR AUDIENCIA ---
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
