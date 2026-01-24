package com.andervalla.gateway.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

// @Configuration
// public class SecurityConfig {

//     @Bean
//     public SecurityFilterChain securityFilterChain(HttpSecurity http, GatewayLoggingFilter gatewayLoggingFilter) throws Exception {
//         http.authorizeHttpRequests(auth -> auth
//                         .requestMatchers("/actuator/health").permitAll()
//                         .requestMatchers("/auth/**").permitAll()
//                         .requestMatchers("/oauth2/**").permitAll()
//                         .requestMatchers("/login/**").permitAll()
//                         .anyRequest().authenticated()
//                 )
//                 .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                 .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
//                 .csrf(AbstractHttpConfigurer::disable)
//                 .addFilterBefore(gatewayLoggingFilter, UsernamePasswordAuthenticationFilter.class);
//         return http.build();
//     }

//     @Bean
//     public GatewayLoggingFilter gatewayLoggingFilter() {
//         return new GatewayLoggingFilter();
//     }
// }

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, GatewayLoggingFilter gatewayLoggingFilter)
            throws Exception {
        http
                // 1. AÑADIR ESTA LÍNEA AQUÍ: Habilitar CORS usando la configuración definida
                // abajo
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/oauth2/**").permitAll()
                        .requestMatchers("/login/**").permitAll()
                        // Agrega OPTIONS por si acaso el navegador manda preflight checks
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().authenticated())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(gatewayLoggingFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    // 2. AÑADIR ESTE BEAN AL FINAL DE LA CLASE
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // IMPORTANTE: Pon aquí tu URL de Azure EXACTA (sin la 'h' extra que corregimos
        // antes)
        // También deja localhost para cuando pruebes en local
        configuration.setAllowedOrigins(Arrays.asList(
                "https://gateway.orangestone-4ddca4b7.eastus2.azurecontainerapps.io",
                "https://denuncia-segura-frontend.vercel.app",
                "http://localhost:4200"));

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public GatewayLoggingFilter gatewayLoggingFilter() {
        return new GatewayLoggingFilter();
    }
}
