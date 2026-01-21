package com.andervalla.gateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE) // Esto le da prioridad máxima sobre la seguridad
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Permite llamadas desde tu Angular
        config.setAllowedOrigins(List.of("http://localhost:4200"));

        // Permite todos los métodos necesarios
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Permite todas las cabeceras
        config.setAllowedHeaders(Arrays.asList("*"));

        // Permite cookies/credenciales (Vital para OAuth)
        config.setAllowCredentials(true);

        // Cuánto tiempo recordar esta config (1 hora)
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
