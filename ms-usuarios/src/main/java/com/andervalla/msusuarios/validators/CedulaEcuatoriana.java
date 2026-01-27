package com.andervalla.msusuarios.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/** Anotación de Bean Validation para validar cédula ecuatoriana (10 dígitos). */
@Documented
@Constraint(validatedBy = CedulaEcuatorianaValidator.class)
@Target({FIELD})
@Retention(RUNTIME)
public @interface CedulaEcuatoriana {
    String message() default "Cedula ecuatoriana invalida";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
