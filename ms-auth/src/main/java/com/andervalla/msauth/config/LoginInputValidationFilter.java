package com.andervalla.msauth.config;

import com.andervalla.msauth.services.security.PasswordPolicyValidator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;
import org.springframework.http.HttpMethod;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Valida los campos del login antes de autenticar.
 */
public class LoginInputValidationFilter extends OncePerRequestFilter {

    private static final Pattern CEDULA_PATTERN = Pattern.compile("^\\d{10}$");
    private final PasswordPolicyValidator passwordPolicyValidator;

    public LoginInputValidationFilter(PasswordPolicyValidator passwordPolicyValidator) {
        this.passwordPolicyValidator = passwordPolicyValidator;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !HttpMethod.POST.matches(request.getMethod()) || !"/login".equals(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (username == null || !CEDULA_PATTERN.matcher(username).matches()) {
            response.sendRedirect("/login?error=validation");
            return;
        }

        try {
            passwordPolicyValidator.validate(password);
        } catch (RuntimeException ex) {
            response.sendRedirect("/login?error=validation");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
