package com.andervalla.msusuarios.controllers.dtos.requests;

import com.andervalla.msusuarios.models.enums.EntidadEnum;
import com.andervalla.msusuarios.models.enums.RolEnum;
import com.andervalla.msusuarios.validators.CedulaEcuatoriana;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegistroStaffRequest(
        @NotBlank
        @Email
        @Size(max = 120)
        String email,
        @NotBlank
        @Size(max = 160)
        String nombre,
        @NotBlank
        @Size(min = 10, max = 10)
        @CedulaEcuatoriana
        String cedula,
        @NotNull
        RolEnum rol,
        @NotNull
        EntidadEnum entidad
) {
}
