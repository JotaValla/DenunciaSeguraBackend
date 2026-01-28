package com.andervalla.msdenuncias.services.mappers;

import com.andervalla.msdenuncias.clients.UsuariosClient;
import com.andervalla.msdenuncias.clients.dtos.EvidenciaDTO;
import com.andervalla.msdenuncias.clients.dtos.UsuarioDTO;
import com.andervalla.msdenuncias.controllers.dtos.responses.DenunciaEstadoHistorialResponse;
import com.andervalla.msdenuncias.controllers.dtos.responses.DenunciaListadoResponse;
import com.andervalla.msdenuncias.controllers.dtos.responses.DenunciaResponse;
import com.andervalla.msdenuncias.controllers.dtos.responses.EstadoCambio;
import com.andervalla.msdenuncias.controllers.dtos.responses.Reporter;
import com.andervalla.msdenuncias.models.DenunciaEntity;
import com.andervalla.msdenuncias.models.DenunciaEstadoHistorialEntity;
import com.andervalla.msdenuncias.models.enums.NivelAnonimatoEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public final class DenunciaMapper {

    private final UsuariosClient usuariosClient;

    public DenunciaResponse toDenunciaResponseDTO(DenunciaEntity denunciaEntity, List<EvidenciaDTO> evidenciasExternas, List<EvidenciaDTO> evidenciasResolucionExternas) {

        List<EvidenciaDTO> evidenciasFinales = (evidenciasExternas != null) ? evidenciasExternas : List.of();
        List<EvidenciaDTO> evidenciasResolucionFinales = (evidenciasResolucionExternas != null) ? evidenciasResolucionExternas : List.of();

        Reporter ciudadano = buildCiudadanoReporter(denunciaEntity);
        Reporter operador = buildStaffReporter(denunciaEntity.getOperadorId());
        Reporter jefe = buildStaffReporter(denunciaEntity.getJefeId());

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
                ciudadano,
                operador,
                jefe,
                denunciaEntity.getComentarioResolucion(),
                denunciaEntity.getComentarioObservacion(),
                evidenciasFinales,
                evidenciasResolucionFinales,
                denunciaEntity.getCreadoEn(),
                denunciaEntity.getActualizadoEn()
        );
    }

    public DenunciaListadoResponse toDenunciaListadoResponseDTO(DenunciaEntity denunciaEntity) {
        Reporter ciudadano = buildCiudadanoReporter(denunciaEntity);
        Reporter operador = buildStaffReporter(denunciaEntity.getOperadorId());
        Reporter jefe = buildStaffReporter(denunciaEntity.getJefeId());

        return new DenunciaListadoResponse(
                denunciaEntity.getId(),
                denunciaEntity.getTitulo(),
                ciudadano,
                operador,
                jefe,
                denunciaEntity.getCreadoEn(),
                denunciaEntity.getEntidadResponsable(),
                denunciaEntity.getEstadoDenunciaEnum(),
                denunciaEntity.getCategoriaDenunciaEnum()
        );
    }


    public DenunciaEstadoHistorialResponse toDenunciaEstadoHistorialResponseDTO(DenunciaEntity denunciaEncontrada, List<DenunciaEstadoHistorialEntity> historialEntities) {

        List<EstadoCambio> estadoCambios = historialEntities.stream()
                .map(historialEntity -> new EstadoCambio(
                        historialEntity.getEstadoAnterior(),
                        historialEntity.getEstadoAtual(),
                        resolverNombreActor(historialEntity.getActorId()),
                        historialEntity.getOcurridoEn()
                ))
                .toList();

        return new DenunciaEstadoHistorialResponse(
                denunciaEncontrada.getId(),
                estadoCambios
        );

    }

    private Reporter buildCiudadanoReporter(DenunciaEntity denunciaEntity) {
        if (denunciaEntity.getNivelAnonimatoEnum() == NivelAnonimatoEnum.PSEUDOANONIMO && denunciaEntity.getHashIdentidad() != null) {
            String alias = (denunciaEntity.getAliasPseudo() != null && !denunciaEntity.getAliasPseudo().isBlank())
                    ? denunciaEntity.getAliasPseudo()
                    : "Alias reservado";
            return new Reporter("Información Privada", alias);
        }
        if (denunciaEntity.getCiudadanoId() != null) {
            try {
                UsuarioDTO usuario = usuariosClient.obtenerUsuarioPorId(denunciaEntity.getCiudadanoId());
                return new Reporter(usuario.nombre(), null);
            } catch (Exception e) {
                log.warn("No se pudo obtener datos del ciudadano {} para reporte", denunciaEntity.getCiudadanoId(), e);
                return new Reporter("Ciudadano", null);
            }
        }
        return new Reporter("Información no disponible", null);
    }

    private Reporter buildStaffReporter(Long staffId) {
        if (staffId == null) {
            return null;
        }
        try {
            UsuarioDTO usuario = usuariosClient.obtenerUsuarioPorId(staffId);
            return new Reporter(usuario.nombre(), null);
        } catch (Exception e) {
            log.warn("No se pudo obtener datos de staff {}", staffId, e);
            return new Reporter("Staff", null);
        }
    }

    private String resolverNombreActor(Long actorId) {
        if (actorId == null) {
            return "Información no disponible";
        }
        if (actorId == 0L) {
            return "Información Privada";
        }
        try {
            UsuarioDTO usuario = usuariosClient.obtenerUsuarioPorId(actorId);
            return usuario.nombre();
        } catch (Exception e) {
            log.warn("No se pudo obtener nombre de actor {}", actorId, e);
            return "Actor";
        }
    }
}
