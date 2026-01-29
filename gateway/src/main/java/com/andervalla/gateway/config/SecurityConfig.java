package com.andervalla.gateway.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Configura la seguridad del gateway: expone rutas públicas de autenticación y
 * salud, exige JWT para el resto, desactiva CSRF en un entorno stateless y define
 * CORS para los orígenes permitidos.
 */
@Configuration
@EnableConfigurationProperties({CorsProperties.class, TokenCookieProperties.class})
public class SecurityConfig {

    private final CorsProperties corsProperties;
    private final TokenCookieProperties tokenCookieProperties;

    public SecurityConfig(CorsProperties corsProperties, TokenCookieProperties tokenCookieProperties) {
        this.corsProperties = corsProperties;
        this.tokenCookieProperties = tokenCookieProperties;
    }

    /**
     * Define las reglas de autorización (rutas públicas vs protegidas), habilita CORS
     * y configura el gateway como recurso protegido con JWT en modo stateless
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/.well-known/**").permitAll()
                        .requestMatchers("/auth/.well-known/**").permitAll()
                        // Agrega OPTIONS por si acaso el navegador manda preflight checks
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().authenticated())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .bearerTokenResolver(bearerTokenResolver())
                        .jwt(Customizer.withDefaults()))
                .addFilterBefore(new CookieBearerTokenFilter(tokenCookieProperties), BearerTokenAuthenticationFilter.class)
                .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }

    /**
     * Configuración CORS: orígenes, métodos y cabeceras permitidas para llamadas
     * desde navegadores.
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(corsProperties.getAllowedOrigins());

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept",
                "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Resuelve el token Bearer desde Authorization o desde cookie.
     */
    @Bean
    public BearerTokenResolver bearerTokenResolver() {
        DefaultBearerTokenResolver resolver = new DefaultBearerTokenResolver();
        return request -> {
            String uri = request.getRequestURI();
            if (uri != null && (uri.equals("/auth") || uri.startsWith("/auth/") || uri.startsWith("/.well-known/"))) {
                return null;
            }
            String token = resolver.resolve(request);
            if (token != null) {
                return token;
            }
            return new CookieBearerTokenResolver(tokenCookieProperties).resolve(request);
        };
    }
}
