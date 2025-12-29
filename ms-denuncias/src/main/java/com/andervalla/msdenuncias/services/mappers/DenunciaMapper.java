package com.andervalla.msdenuncias.services.mappers;

import com.andervalla.msdenuncias.clients.dtos.EvidenciaDTO;
import com.andervalla.msdenuncias.controllers.dtos.responses.DenunciaEstadoHistorialResponse;
import com.andervalla.msdenuncias.controllers.dtos.responses.DenunciaResponse;
import com.andervalla.msdenuncias.controllers.dtos.responses.EstadoCambio;
import com.andervalla.msdenuncias.models.DenunciaEntity;
import com.andervalla.msdenuncias.models.DenunciaEstadoHistorialEntity;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@NoArgsConstructor
@Component
public final class DenunciaMapper {

    public  DenunciaResponse toDenunciaResponseDTO(DenunciaEntity denunciaEntity, List<EvidenciaDTO> evidenciasExternas, List<EvidenciaDTO> evidenciasResolucionExternas) {

        List<EvidenciaDTO> evidenciasFinales = (evidenciasExternas != null) ? evidenciasExternas : List.of();
        List<EvidenciaDTO> evidenciasResolucionFinales = (evidenciasResolucionExternas != null) ? evidenciasResolucionExternas : List.of();

        return new DenunciaResponse(
                denunciaEntity.getId(),
                denunciaEntity.getTitulo(),
                denunciaEntity.getDescripcion(),
                denunciaEntity.getCategoriaDenunciaEnum(),
                denunciaEntity.getEntidadResponsable(),
                denunciaEntity.getLatitud(),
                denunciaEntity.getLongitud(),
                denunciaEntity.getNivelAnonimatoEnum(),
                denunciaEntity.getEstadoDenunciaEnum(),
                denunciaEntity.getCiudadanoId(),
                denunciaEntity.getOperadorId(),
                denunciaEntity.getComentarioResolucion(),
                denunciaEntity.getComentarioObservacion(),
                evidenciasFinales,
                evidenciasResolucionFinales,
                denunciaEntity.getCreadoEn(),
                denunciaEntity.getActualizadoEn()
        );
    }


    public DenunciaEstadoHistorialResponse toDenunciaEstadoHistorialResponseDTO(DenunciaEntity denunciaEncontrada, List<DenunciaEstadoHistorialEntity> historialEntities) {

        List<EstadoCambio> estadoCambios = historialEntities.stream()
                .map(historialEntity -> new EstadoCambio(
                        historialEntity.getEstadoAnterior(),
                        historialEntity.getEstadoAtual(),
                        historialEntity.getActorId(),
                        historialEntity.getOcurridoEn()
                ))
                .toList();

        return new DenunciaEstadoHistorialResponse(
                denunciaEncontrada.getId(),
                estadoCambios
        );

    }
}
