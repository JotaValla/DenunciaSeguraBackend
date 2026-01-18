package com.andervalla.msdenuncias.controllers.dtos.requests;

import com.andervalla.msdenuncias.models.enums.CategoriaDenunciaEnum;
import com.andervalla.msdenuncias.models.enums.NivelAnonimatoEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;
import java.util.List;

public record CrearDenunciaRequest(
        @NotBlank @Size(min = 5, max = 100) String titulo,
        @NotBlank @Size(min = 10, max = 500) String descripcion,
        @NotBlank CategoriaDenunciaEnum categoriaDenuncia,
        @NotBlank @DecimalMin("-90.0") @DecimalMax("90.0") Double latitud,
        @NotBlank @DecimalMin("-180.0") @DecimalMax("180.0") Double longitud,
        @NotBlank NivelAnonimatoEnum nivelAnonimato,
        @NotNull List<String> evidenciasIds
){}
