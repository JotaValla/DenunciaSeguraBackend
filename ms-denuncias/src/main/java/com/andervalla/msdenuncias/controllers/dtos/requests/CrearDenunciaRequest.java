package com.andervalla.msdenuncias.controllers.dtos.requests;

import com.andervalla.msdenuncias.models.enums.CategoriaDenunciaEnum;
import com.andervalla.msdenuncias.models.enums.NivelAnonimatoEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CrearDenunciaRequest(
        @NotNull @NotBlank  String titulo,
        @NotBlank @NotNull String descripcion,
        @NotNull CategoriaDenunciaEnum categoriaDenuncia,
        @NotNull Double latitud,
        @NotNull Double longitud,
        @NotNull NivelAnonimatoEnum nivelAnonimato,
        List<String> evidenciasIds
){}
