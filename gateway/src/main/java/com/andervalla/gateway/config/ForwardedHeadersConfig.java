package com.andervalla.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ForwardedHeaderFilter;

@Configuration
public class ForwardedHeadersConfig {

    /**
     * Trust X-Forwarded-* headers injected by Azure Container Apps / ingress so
     * downstream apps (like ms-auth) build redirects with the public host.
     */
    @Bean
    public ForwardedHeaderFilter forwardedHeaderFilter() {
        ForwardedHeaderFilter filter = new ForwardedHeaderFilter();
        filter.setRemoveOnly(false);
        return filter;
    }
}
