package com.andervalla.msauth.services.security;

import com.andervalla.msauth.exceptions.PasswordPolicyException;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Valida complejidad de contraseñas y las compara contra listas comunes filtradas.
 */
@Component
public class PasswordPolicyValidator {

    private static final Pattern COMPLEXITY = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w\\s]).{8,}$");
    private static final Set<String> COMMON_PASSWORDS = Set.of(
            "123456", "123456789", "qwerty", "password", "111111", "12345678",
            "abc123", "1234567", "password1", "12345", "123123", "admin", "iloveyou"
    );

    /**
     * Lanza {@link com.andervalla.msauth.exceptions.PasswordPolicyException} si la contraseña no cumple los criterios.
     */
    public void validate(String rawPassword) {
        if (rawPassword == null || !COMPLEXITY.matcher(rawPassword).matches()) {
            throw new PasswordPolicyException("La contraseña debe tener al menos 8 caracteres, mayúsculas, minúsculas, dígitos y un símbolo.");
        }
        if (COMMON_PASSWORDS.contains(rawPassword.toLowerCase())) {
            throw new PasswordPolicyException("La contraseña elegida es demasiado común.");
        }
    }
}
