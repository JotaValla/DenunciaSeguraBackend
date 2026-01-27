package com.andervalla.msauth.config;

import com.andervalla.msauth.services.CaptchaService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class CaptchaFilter extends OncePerRequestFilter {

    private final CaptchaService captchaService;

    public CaptchaFilter(CaptchaService captchaService) {
        this.captchaService = captchaService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Interceptamos SOLO el inicio del flujo OAuth (/oauth2/authorize)
        // Angular envía el usuario aquí con ?client_id=...&captcha_token=...
        if (request.getRequestURI().startsWith("/oauth2/authorize")) {

            // 2. Extraemos el token que enviamos desde Angular
            String captchaToken = request.getParameter("captcha_token");

            // 3. Validamos
            if (!captchaService.validarToken(captchaToken)) {
                // Si falla, devolvemos error 400 y cortamos la cadena.
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Captcha inválido o requerido\"}");
                return; // IMPORTANTE: No llamamos a filterChain.doFilter
            }
        }

        // Si no es la ruta de authorize o el captcha es válido, continuamos
        filterChain.doFilter(request, response);
    }
}
