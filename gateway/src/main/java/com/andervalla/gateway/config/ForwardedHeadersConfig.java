package com.andervalla.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ForwardedHeaderFilter;

@Configuration
public class ForwardedHeadersConfig {

    /**
     * Activa un filtro que lee las cabeceras {@code X-Forwarded-*} añadidas por
     * balanceadores o proxies para reconstruir el host, puerto y protocolo que el
     * cliente usó realmente. Así, el gateway genera redirecciones y enlaces con la
     * URL pública correcta y no con direcciones internas.
     */
    @Bean
    public ForwardedHeaderFilter forwardedHeaderFilter() {
        ForwardedHeaderFilter filter = new ForwardedHeaderFilter();
        filter.setRemoveOnly(false);
        return filter;
    }
}
