package com.andervalla.msdenuncias.services;

import com.andervalla.msdenuncias.clients.EvidenciasClient;
import com.andervalla.msdenuncias.clients.dtos.AdjuntarEvidenciaRequest;
import com.andervalla.msdenuncias.clients.dtos.EvidenciaDTO;
import com.andervalla.msdenuncias.controllers.dtos.requests.AsignarOperadorRequest;
import com.andervalla.msdenuncias.controllers.dtos.requests.CrearDenunciaRequest;
import com.andervalla.msdenuncias.controllers.dtos.requests.MarcarResolucionRequest;
import com.andervalla.msdenuncias.controllers.dtos.requests.ValidarSolucionRequest;
import com.andervalla.msdenuncias.controllers.dtos.responses.DenunciaEstadoHistorialResponse;
import com.andervalla.msdenuncias.controllers.dtos.responses.DenunciaResponse;
import com.andervalla.msdenuncias.controllers.dtos.responses.DenunciaResumenResponse;
import com.andervalla.msdenuncias.exceptions.ComentarioObservacionRequeridoException;
import com.andervalla.msdenuncias.exceptions.DenunciaEstadoInvalidoException;
import com.andervalla.msdenuncias.exceptions.DenunciaNotFoundException;
import com.andervalla.msdenuncias.exceptions.EntidadResponsableNoAsignadaException;
import com.andervalla.msdenuncias.exceptions.EntidadResponsableYaAsignadaException;
import com.andervalla.msdenuncias.exceptions.EvidenciasRequeridasException;
import com.andervalla.msdenuncias.models.DenunciaAsignacionEntity;
import com.andervalla.msdenuncias.models.DenunciaEntity;
import com.andervalla.msdenuncias.models.DenunciaEstadoHistorialEntity;
import com.andervalla.msdenuncias.models.DenunciaResolucionEntity;
import com.andervalla.msdenuncias.models.DenunciaValidacionEntity;
import com.andervalla.msdenuncias.models.enums.EntidadResponsableEnum;
import com.andervalla.msdenuncias.models.enums.EstadoDenunciaEnum;
import com.andervalla.msdenuncias.repositories.*;
import com.andervalla.msdenuncias.services.mappers.DenunciaMapper;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.internal.util.logging.Log;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
public class DenunciaServiceImpl implements IDenunciaService {

    private final DenunciaRepository denunciaRepository;
    private final DenunciaEstadoHistorialRepository denunciaEstadoHistorialRepository;
    private final DenunciaAsignacionRepository denunciaAsignacionRepository;
    private final DenunciaResolucionRepository denunciaResolucionRepository;
    private final DenunciaValidacionRepository denunciaValidacionRepository;
    private final DenunciaMapper denunciaMapper;

    private final EvidenciasClient evidenciasClient;

    public DenunciaServiceImpl(
            DenunciaRepository denunciaRepository,
            DenunciaEstadoHistorialRepository denunciaEstadoHistorialRepository,
            DenunciaAsignacionRepository denunciaAsignacionRepository,
            DenunciaResolucionRepository denunciaResolucionRepository,
            DenunciaValidacionRepository denunciaValidacionRepository,
            DenunciaMapper denunciaMapper,
            EvidenciasClient evidenciasClient
    ) {
        this.denunciaRepository = denunciaRepository;
        this.denunciaEstadoHistorialRepository = denunciaEstadoHistorialRepository;
        this.denunciaAsignacionRepository = denunciaAsignacionRepository;
        this.denunciaResolucionRepository = denunciaResolucionRepository;
        this.denunciaValidacionRepository = denunciaValidacionRepository;
        this.denunciaMapper = denunciaMapper;
        this.evidenciasClient = evidenciasClient;
    }

    @Override
    @Transactional
    public DenunciaResumenResponse crearDenuncia(CrearDenunciaRequest denunciaReq) {

        //1. Determinar la entidad responsable de la denuncia
        EntidadResponsableEnum entRespAsignada = determinarEntidadPorCategoria(denunciaReq);

        String evidenciasString = null;

        if (denunciaReq.evidenciasIds() != null && !denunciaReq.evidenciasIds().isEmpty()) {
            evidenciasString = String.join(",", denunciaReq.evidenciasIds());
        }

        //2. Crear la denuncia
        DenunciaEntity denunciaEntity = DenunciaEntity.builder()
                .titulo(denunciaReq.titulo())
                .descripcion(denunciaReq.descripcion())
                .categoriaDenunciaEnum(denunciaReq.categoriaDenuncia())
                .latitud(denunciaReq.latitud())
                .longitud(denunciaReq.longitud())
                .evidenciasIds(evidenciasString)
                .nivelAnonimatoEnum(denunciaReq.nivelAnonimato())
                .ciudadanoId(denunciaReq.ciudadanoId())
                .entidadResponsable(entRespAsignada)
                .estadoDenunciaEnum(EstadoDenunciaEnum.RECIBIDA)
                .build();

        //3. Guardar la denuncia
        DenunciaEntity denunciaGuardada = denunciaRepository.save(denunciaEntity);

        //4. Registrar el cambio de estado
        registrarCambioEstado(denunciaGuardada, null, denunciaGuardada.getEstadoDenunciaEnum(), denunciaGuardada.getCiudadanoId());

        //5. Vincular las evidencias a la denuncia en el microservicio externo
        try {
            evidenciasClient.adjuntarEvidencias(AdjuntarEvidenciaRequest.builder()
                    .entidadTipo("DENUNCIA")
                    .entidadId(denunciaGuardada.getId())
                    .evidenciasIds(denunciaReq.evidenciasIds())
                    .usuarioId(denunciaReq.ciudadanoId())
                    .build()
            );
        } catch (Exception e) {
            // Si falla la vinculación, lanzamos excepción para hacer rollback de la denuncia
            throw new RuntimeException("Error al vincular evidencias: " + e.getMessage());
        }

        //6. Retornar el resumen de la denuncia creada
        return new DenunciaResumenResponse(denunciaGuardada.getId(), "Denuncia creada");
    }

    @Override
    @Transactional(readOnly = true)
    public DenunciaResponse obtenerDenuncia(Long denunciaId) {
        DenunciaEntity denunciaEncontrada = denunciaRepository.findById(denunciaId)
                .orElseThrow(() -> new DenunciaNotFoundException(denunciaId));

        // 1. Obtener Evidencias del Microservicio Externo
        List<EvidenciaDTO> evidenciasCiudadano = List.of();
        try {
            // Llamada Feign Client
            evidenciasCiudadano = evidenciasClient.obtenerEvidencias("DENUNCIA", denunciaId);
        } catch (Exception e) {
            evidenciasCiudadano = List.of();
        }

        // 2. Obtener Evidencias del OPERADOR
        List<EvidenciaDTO> evidenciasResolucion = List.of();
        if (denunciaEncontrada.getEstadoDenunciaEnum() == EstadoDenunciaEnum.EN_VALIDACION ||
                denunciaEncontrada.getEstadoDenunciaEnum() == EstadoDenunciaEnum.RESUELTA) {
            try {
                evidenciasResolucion = evidenciasClient.obtenerEvidencias("RESOLUCION", denunciaId);
            } catch (Exception e) {
                System.out.println(e);
            }
        }

        // 3. Llamar al Mapper pasando AMBOS datos (Entidad + Evidencias)
        return denunciaMapper.toDenunciaResponseDTO(denunciaEncontrada, evidenciasCiudadano, evidenciasResolucion);

    }

    @Override
    @Transactional
    public void asignarDenunciaEntidadResponsableSupervisor(Long denunciaId, EntidadResponsableEnum entidadResponsableEnum) {

        // 1. Buscar la denuncia
        DenunciaEntity denunciaEncontrada = denunciaRepository.findById(denunciaId)
                .orElseThrow(() -> new DenunciaNotFoundException(denunciaId));

        // 2. Validar que la denuncia NO tenga una entidad responsable asignada
        if (denunciaEncontrada.getEntidadResponsable() != null) {
            throw new EntidadResponsableYaAsignadaException(denunciaId);
        }

        // 3. Asignar la entidad responsable
        denunciaEncontrada.setEntidadResponsable(entidadResponsableEnum);

        // 4. Guardar la denuncia actualizada
        denunciaRepository.save(denunciaEncontrada);

    }

    @Override
    @Transactional
    public void asignarDenunciaOperador(Long denunciaId, AsignarOperadorRequest asignarOperadorADenuncia) {

        // 1. Buscar la denuncia
        DenunciaEntity denunciaEncontrada = denunciaRepository.findById(denunciaId)
                .orElseThrow(() -> new DenunciaNotFoundException(denunciaId));

        // 2. Validar que la denuncia exista
        if (denunciaEncontrada.getEntidadResponsable() == null) {
            throw new EntidadResponsableNoAsignadaException(denunciaId);
        }

        // 3. Crear el registro de asignacion
        DenunciaAsignacionEntity asignacion = DenunciaAsignacionEntity.builder()
                .denuncia(denunciaEncontrada)
                .operadorAnteriorId(null)
                .operadorNuevoId(asignarOperadorADenuncia.operadorId())
                .asignadoPorId(asignarOperadorADenuncia.asignadoPorId())
                .ocurridoEn(Instant.now())
                .build();

        //4. Guardar el registro de asignacion
        EstadoDenunciaEnum estadoAnterior = denunciaEncontrada.getEstadoDenunciaEnum();
        denunciaEncontrada.setEstadoDenunciaEnum(EstadoDenunciaEnum.ASIGNADA);
        registrarCambioEstado(denunciaEncontrada, estadoAnterior, EstadoDenunciaEnum.ASIGNADA, asignarOperadorADenuncia.asignadoPorId());

        //5. Guardar la denuncia actualizada y la asignacion
        denunciaEncontrada.setOperadorId(asignarOperadorADenuncia.operadorId());
        denunciaRepository.save(denunciaEncontrada);

        //6. Guardar la asignacion
        denunciaAsignacionRepository.save(asignacion);

    }

    @Override
    public void iniciarProcesoDenunciaOperadores(Long denunciaId, Long operadorId) {
        //1. Buscar la denuncia
        DenunciaEntity denunciaEncontrada = denunciaRepository.findById(denunciaId)
                .orElseThrow(() -> new DenunciaNotFoundException(denunciaId));

        //2. Validar que la denuncia este en estado ASIGNADA
        if (denunciaEncontrada.getEstadoDenunciaEnum() != EstadoDenunciaEnum.ASIGNADA || denunciaEncontrada.getEstadoDenunciaEnum() == EstadoDenunciaEnum.EN_PROCESO) {
            throw new DenunciaEstadoInvalidoException(denunciaId,
                    EstadoDenunciaEnum.ASIGNADA,
                    denunciaEncontrada.getEstadoDenunciaEnum());
        }

        //3. Actualizar el estado de la denuncia a EN_PROCESO
        EstadoDenunciaEnum estadoAnterior = denunciaEncontrada.getEstadoDenunciaEnum();
        denunciaEncontrada.setEstadoDenunciaEnum(EstadoDenunciaEnum.EN_PROCESO);
        registrarCambioEstado(denunciaEncontrada, estadoAnterior, EstadoDenunciaEnum.EN_PROCESO, operadorId);

        //4. Guardar la denuncia actualizada
        denunciaRepository.save(denunciaEncontrada);
    }

    @Override
    @Transactional
    public void resolverDenunciaOperador(Long denunciaId, MarcarResolucionRequest marcarResolucionDenuncia) {

        //1. Buscar la denuncia
        DenunciaEntity denunciaEncontrada = denunciaRepository.findById(denunciaId)
                .orElseThrow(() -> new DenunciaNotFoundException(denunciaId));

        //2. Validar que la denuncia este en estado EN_PROCESO
        if (denunciaEncontrada.getEstadoDenunciaEnum() != EstadoDenunciaEnum.EN_PROCESO) {
            throw new DenunciaEstadoInvalidoException(denunciaId,
                    EstadoDenunciaEnum.EN_PROCESO,
                    denunciaEncontrada.getEstadoDenunciaEnum());
        }

        //3. Validar que si exista evidencia
        if (marcarResolucionDenuncia.evidenciasIds() == null || marcarResolucionDenuncia.evidenciasIds().isEmpty()) {
            throw new EvidenciasRequeridasException(denunciaId);
        }

        //4. Crear el registro de resolucion
        DenunciaResolucionEntity resolucion = DenunciaResolucionEntity.builder()
                .denuncia(denunciaEncontrada)
                .comentarioResolucion(marcarResolucionDenuncia.comentarioResolucion())
                .resueltoPorId(marcarResolucionDenuncia.resueltoPorId())
                .evienciaIds(String.join(",", marcarResolucionDenuncia.evidenciasIds()))
                .ocurridoEn(Instant.now())
                .build();

        //5. Guardar el comentario de resolucion en la entidad Denuncia
        denunciaEncontrada.setComentarioResolucion(marcarResolucionDenuncia.comentarioResolucion());

        //6. Guardar el registro de resolucion
        denunciaResolucionRepository.save(resolucion);

        //7. Vincular las evidencias a la denuncia
        try {
            evidenciasClient.adjuntarEvidencias(AdjuntarEvidenciaRequest.builder()
                    .entidadTipo("RESOLUCION")
                    .entidadId(denunciaId)
                    .evidenciasIds(marcarResolucionDenuncia.evidenciasIds())
                    .usuarioId(marcarResolucionDenuncia.resueltoPorId())
                    .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Error al vincular evidencias de resolución: " + e.getMessage());
        }

        //8. Actualizar el estado de la denuncia a EN_VALIDACION
        EstadoDenunciaEnum estadoAnterior = denunciaEncontrada.getEstadoDenunciaEnum();
        denunciaEncontrada.setEstadoDenunciaEnum(EstadoDenunciaEnum.EN_VALIDACION);

        registrarCambioEstado(denunciaEncontrada, estadoAnterior, EstadoDenunciaEnum.EN_VALIDACION, marcarResolucionDenuncia.resueltoPorId());
        denunciaRepository.save(denunciaEncontrada);

    }

    @Override
    @Transactional
    public void validarDenunciaJefe(Long denunciaId, ValidarSolucionRequest validarSolucionDenuncia) {

        //1. Buscar la denuncia
        DenunciaEntity denunciaEncontrada = denunciaRepository.findById(denunciaId)
                .orElseThrow(() -> new DenunciaNotFoundException(denunciaId));

        //2. Validar que la denuncia este en estado EN_VALIDACION
        if (denunciaEncontrada.getEstadoDenunciaEnum() != EstadoDenunciaEnum.EN_VALIDACION) {
            throw new DenunciaEstadoInvalidoException(denunciaId,
                    EstadoDenunciaEnum.EN_VALIDACION,
                    denunciaEncontrada.getEstadoDenunciaEnum());
        }

        if (validarSolucionDenuncia.comentarioObservacion() == null) {
            throw new ComentarioObservacionRequeridoException(denunciaId);
        }

        //3. Registrar una validacion aprobada
        if (validarSolucionDenuncia.aprobada()) {
            //Actualizar el estado de la denuncia a RESUELTA
            EstadoDenunciaEnum estadoAnterior = denunciaEncontrada.getEstadoDenunciaEnum();
            denunciaEncontrada.setEstadoDenunciaEnum(EstadoDenunciaEnum.RESUELTA);
            denunciaEncontrada.setComentarioObservacion(validarSolucionDenuncia.comentarioObservacion());
            registrarCambioEstado(denunciaEncontrada, estadoAnterior, EstadoDenunciaEnum.RESUELTA, validarSolucionDenuncia.validadoPorId());
            denunciaRepository.save(denunciaEncontrada);
            // Crear el registro de validacion
            DenunciaValidacionEntity validacion = DenunciaValidacionEntity.builder()
                    .denuncia(denunciaEncontrada)
                    .aprobada(true)
                    .comentarioObservacion(validarSolucionDenuncia.comentarioObservacion())
                    .validadoPorId(validarSolucionDenuncia.validadoPorId())
                    .ocurridoEn(Instant.now())
                    .build();
            // Guardar el registro de validacion
            denunciaValidacionRepository.save(validacion);
        } else {
            //Registrar una validacion no aprobada
            //Actualizar el estado de la denuncia a EN_PROCESO
            EstadoDenunciaEnum estadoAnterior = denunciaEncontrada.getEstadoDenunciaEnum();
            denunciaEncontrada.setEstadoDenunciaEnum(EstadoDenunciaEnum.EN_PROCESO);
            denunciaEncontrada.setComentarioObservacion(validarSolucionDenuncia.comentarioObservacion());
            registrarCambioEstado(denunciaEncontrada, estadoAnterior, EstadoDenunciaEnum.EN_PROCESO, validarSolucionDenuncia.validadoPorId());
            denunciaRepository.save(denunciaEncontrada);
            // Crear el registro de validacion
            DenunciaValidacionEntity validacion = DenunciaValidacionEntity.builder()
                    .denuncia(denunciaEncontrada)
                    .aprobada(false)
                    .comentarioObservacion(validarSolucionDenuncia.comentarioObservacion())
                    .validadoPorId(validarSolucionDenuncia.validadoPorId())
                    .ocurridoEn(Instant.now())
                    .build();
            // Guardar el registro de validacion
            denunciaValidacionRepository.save(validacion);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DenunciaEstadoHistorialResponse historialDenuncia(Long denunciaId) {
        DenunciaEntity denunciaEncontrada = denunciaRepository.findById(denunciaId)
                .orElseThrow(() -> new DenunciaNotFoundException(denunciaId));
        List<DenunciaEstadoHistorialEntity> historialEntities =
                denunciaEstadoHistorialRepository.findByDenunciaIdOrderByOcurridoEnAsc(denunciaId);
        return denunciaMapper.toDenunciaEstadoHistorialResponseDTO(denunciaEncontrada, historialEntities);
    }

    private void registrarCambioEstado(DenunciaEntity d,
                                       EstadoDenunciaEnum anterior,
                                       EstadoDenunciaEnum nuevo,
                                       Long actorId) {
        DenunciaEstadoHistorialEntity historialDenuncia = new DenunciaEstadoHistorialEntity();
        historialDenuncia.setDenuncia(d);
        historialDenuncia.setEstadoAnterior(anterior);
        historialDenuncia.setEstadoAtual(nuevo);
        historialDenuncia.setActorId(actorId);
        historialDenuncia.setOcurridoEn(Instant.now());
        denunciaEstadoHistorialRepository.save(historialDenuncia);
    }

    private EntidadResponsableEnum determinarEntidadPorCategoria(CrearDenunciaRequest denunciaReq) {
        return switch (denunciaReq.categoriaDenuncia()) {
            case ILUMINACION -> EntidadResponsableEnum.EMPRESA_ELECTRICA;
            case AGUA -> EntidadResponsableEnum.EMPRESA_AGUA_POTABLE;
            case VIALIDAD, SANIDAD, JARDINERIA -> EntidadResponsableEnum.MUNICIPIO;
            case OTROS -> null; // A definir por el supervisor de denuncias
        };
    }

}
