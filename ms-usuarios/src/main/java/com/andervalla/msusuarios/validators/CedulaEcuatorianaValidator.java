package com.andervalla.msusuarios.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CedulaEcuatorianaValidator implements ConstraintValidator<CedulaEcuatoriana, String> {

    @Override
    public boolean isValid(String cedula, ConstraintValidatorContext context) {
        if (cedula == null || cedula.length() != 10) {
            return false;
        }
        if (!cedula.matches("\\d{10}")) {
            return false;
        }
        if (Integer.parseInt(cedula) == 0) {
            return false;
        }

        int[] coeficientes = {2, 1, 2, 1, 2, 1, 2, 1, 2};
        int suma = 0;

        for (int i = 0; i < 9; i++) {
            int digito = Character.getNumericValue(cedula.charAt(i));
            int multiplicacion = digito * coeficientes[i];
            if (multiplicacion >= 10) {
                multiplicacion -= 9;
            }
            suma += multiplicacion;
        }

        int modulo = suma % 10;
        int digitoVerificador = (modulo == 0) ? 0 : 10 - modulo;
        int ultimoDigito = Character.getNumericValue(cedula.charAt(9));
        return digitoVerificador == ultimoDigito;
    }
}
