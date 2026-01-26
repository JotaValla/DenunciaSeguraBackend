package com.andervalla.msauth.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Valida que el campo contenga una cédula ecuatoriana válida (10 dígitos con checksum).
 */
@Documented
@Constraint(validatedBy = CedulaEcuatorianaValidator.class)
@Target({FIELD})
@Retention(RUNTIME)
public @interface CedulaEcuatoriana {
    /**
     * Mensaje de error por defecto.
     */
    String message() default "Cedula ecuatoriana invalida";
    /**
     * Grupos de validación.
     */
    Class<?>[] groups() default {};
    /**
     * Payload de metadatos para validación.
     */
    Class<? extends Payload>[] payload() default {};
}
