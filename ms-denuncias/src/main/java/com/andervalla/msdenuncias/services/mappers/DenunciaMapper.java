package com.andervalla.msdenuncias.services.mappers;

import com.andervalla.msdenuncias.controllers.dtos.responses.DenunciaResponse;
import com.andervalla.msdenuncias.models.DenunciaEntity;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@NoArgsConstructor
@Component
public final class DenunciaMapper {

    public  DenunciaResponse toDenunciaResponseDTO(DenunciaEntity denunciaEntity) {
        List<String> evidenciaCreacion = List.of();

        return new DenunciaResponse(
                denunciaEntity.getId(),
                denunciaEntity.getTitulo(),
                denunciaEntity.getDescripcion(),
                denunciaEntity.getCategoriaDenunciaEnum(),
                denunciaEntity.getLatitud(),
                denunciaEntity.getLongitud(),
                denunciaEntity.getNivelAnonimatoEnum(),
                denunciaEntity.getEstadoDenunciaEnum(),
                denunciaEntity.getCiudadanoId(),
                denunciaEntity.getOperadorId(),
                denunciaEntity.getComentarioResolucion(),
                denunciaEntity.getComentarioObservacion(),
                evidenciaCreacion,
                denunciaEntity.getCreadoEn(),
                denunciaEntity.getActualizadoEn()
        );
    }
}
