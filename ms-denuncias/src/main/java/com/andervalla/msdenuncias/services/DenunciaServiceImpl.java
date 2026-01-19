package com.andervalla.msdenuncias.services;

import com.andervalla.msdenuncias.clients.EvidenciasClient;
import com.andervalla.msdenuncias.clients.dtos.AdjuntarEvidenciaRequest;
import com.andervalla.msdenuncias.clients.dtos.EvidenciaDTO;
import com.andervalla.msdenuncias.clients.UsuariosClient;
import com.andervalla.msdenuncias.clients.dtos.UsuarioDTO;
import com.andervalla.msdenuncias.controllers.dtos.requests.AsignarOperadorRequest;
import com.andervalla.msdenuncias.controllers.dtos.requests.CrearDenunciaRequest;
import com.andervalla.msdenuncias.controllers.dtos.requests.MarcarResolucionRequest;
import com.andervalla.msdenuncias.controllers.dtos.requests.RechazarDenunciaRequest;
import com.andervalla.msdenuncias.controllers.dtos.requests.ValidarSolucionRequest;
import com.andervalla.msdenuncias.controllers.dtos.responses.DenunciaListadoResponse;
import com.andervalla.msdenuncias.controllers.dtos.responses.DenunciaEstadoHistorialResponse;
import com.andervalla.msdenuncias.controllers.dtos.responses.DenunciaResponse;
import com.andervalla.msdenuncias.controllers.dtos.responses.DenunciaResumenResponse;
import com.andervalla.msdenuncias.exceptions.ComentarioObservacionRequeridoException;
import com.andervalla.msdenuncias.exceptions.DenunciaEstadoInvalidoException;
import com.andervalla.msdenuncias.exceptions.DenunciaNotFoundException;
import com.andervalla.msdenuncias.exceptions.EntidadResponsableNoAsignadaException;
import com.andervalla.msdenuncias.exceptions.EntidadResponsableYaAsignadaException;
import com.andervalla.msdenuncias.exceptions.EvidenciasRequeridasException;
import com.andervalla.msdenuncias.exceptions.EvidenciasVinculacionException;
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
import org.springframework.security.access.AccessDeniedException;
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
    private final UsuariosClient usuariosClient;

    public DenunciaServiceImpl(
            DenunciaRepository denunciaRepository,
            DenunciaEstadoHistorialRepository denunciaEstadoHistorialRepository,
            DenunciaAsignacionRepository denunciaAsignacionRepository,
            DenunciaResolucionRepository denunciaResolucionRepository,
            DenunciaValidacionRepository denunciaValidacionRepository,
            DenunciaMapper denunciaMapper,
            EvidenciasClient evidenciasClient,
            UsuariosClient usuariosClient
    ) {
        this.denunciaRepository = denunciaRepository;
        this.denunciaEstadoHistorialRepository = denunciaEstadoHistorialRepository;
        this.denunciaAsignacionRepository = denunciaAsignacionRepository;
        this.denunciaResolucionRepository = denunciaResolucionRepository;
        this.denunciaValidacionRepository = denunciaValidacionRepository;
        this.denunciaMapper = denunciaMapper;
        this.evidenciasClient = evidenciasClient;
        this.usuariosClient = usuariosClient;
    }

    @Override
    @Transactional
    public DenunciaResumenResponse crearDenuncia(CrearDenunciaRequest denunciaReq, Long actorId) {

        //1. Determinar la entidad responsable de la denuncia
        EntidadResponsableEnum entRespAsignada = determinarEntidadPorCategoria(denunciaReq);
        Long jefeId = null;
        if (entRespAsignada != null) {
            try {
                UsuarioDTO jefe = usuariosClient.obtenerJefePorEntidad(entRespAsignada.name());
                jefeId = jefe.id();
            } catch (Exception e) {
                throw new IllegalStateException("No se pudo obtener jefe para la entidad " + entRespAsignada, e);
            }
        }

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
                .ciudadanoId(actorId)
                .jefeId(jefeId)
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
                    .usuarioId(actorId)
                    .build()
            );
        } catch (Exception e) {
            // Si falla la vinculación, lanzamos excepción para hacer rollback de la denuncia
            throw new EvidenciasVinculacionException("Error al vincular evidencias: " + e.getMessage());
        }

        //6. Retornar el resumen de la denuncia creada
        return new DenunciaResumenResponse(denunciaGuardada.getId(), "Denuncia creada");
    }

    @Override
    @Transactional(readOnly = true)
    public DenunciaResponse obtenerDenuncia(Long denunciaId, Long actorId, String rol, String entidad) {
        DenunciaEntity denunciaEncontrada = denunciaRepository.findById(denunciaId)
                .orElseThrow(() -> new DenunciaNotFoundException(denunciaId));
        validarAccesoADenuncia(denunciaEncontrada, actorId, rol, entidad);

        // 1. Obtener Evidencias del Microservicio Externo
        List<EvidenciaDTO> evidenciasCiudadano = List.of();
        try {
            // Llamada Feign Client
            evidenciasCiudadano = evidenciasClient.obtenerEvidencias("DENUNCIA", denunciaId);
        } catch (Exception e) {
            log.warn("No se pudieron obtener evidencias de denuncia {}", denunciaId, e);
            evidenciasCiudadano = List.of();
        }

        // 2. Obtener Evidencias del OPERADOR
        List<EvidenciaDTO> evidenciasResolucion = List.of();
        if (denunciaEncontrada.getEstadoDenunciaEnum() == EstadoDenunciaEnum.EN_VALIDACION ||
                denunciaEncontrada.getEstadoDenunciaEnum() == EstadoDenunciaEnum.RESUELTA) {
            try {
                evidenciasResolucion = evidenciasClient.obtenerEvidencias("RESOLUCION", denunciaId);
            } catch (Exception e) {
                log.warn("No se pudieron obtener evidencias de resolucion {}", denunciaId, e);
            }
        }

        // 3. Llamar al Mapper pasando AMBOS datos (Entidad + Evidencias)
        return denunciaMapper.toDenunciaResponseDTO(denunciaEncontrada, evidenciasCiudadano, evidenciasResolucion);

    }

    @Override
    @Transactional
    public void asignarDenunciaEntidadResponsableSupervisor(Long denunciaId,
                                                            EntidadResponsableEnum entidadResponsableEnum,
                                                            Long actorId,
                                                            String rol) {

        // 1. Buscar la denuncia
        DenunciaEntity denunciaEncontrada = denunciaRepository.findById(denunciaId)
                .orElseThrow(() -> new DenunciaNotFoundException(denunciaId));
        validarNoRechazada(denunciaEncontrada);

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
    public void asignarDenunciaOperador(Long denunciaId,
                                        AsignarOperadorRequest asignarOperadorADenuncia,
                                        Long actorId,
                                        String rol,
                                        String entidad) {

        // 1. Buscar la denuncia
        DenunciaEntity denunciaEncontrada = denunciaRepository.findById(denunciaId)
                .orElseThrow(() -> new DenunciaNotFoundException(denunciaId));
        validarNoRechazada(denunciaEncontrada);

        // 2. Validar que la denuncia exista
        if (denunciaEncontrada.getEntidadResponsable() == null) {
            throw new EntidadResponsableNoAsignadaException(denunciaId);
        }

        // Solo jefes con entidad matching o supervisor/admin pueden asignar
        validarAsignador(denunciaEncontrada, rol, entidad);

        // Validar que el operador pertenezca a la misma entidad y rol correcto
        UsuarioDTO operador = usuariosClient.obtenerUsuarioPorId(asignarOperadorADenuncia.operadorId());
        if (operador.entidad() == null || !operador.entidad().equalsIgnoreCase(denunciaEncontrada.getEntidadResponsable().name())) {
            throw new AccessDeniedException("Operador no pertenece a la entidad de la denuncia");
        }
        if (denunciaEncontrada.getEntidadResponsable() == EntidadResponsableEnum.MUNICIPIO) {
            if (!"OP_INT".equalsIgnoreCase(operador.rol())) {
                throw new AccessDeniedException("Operador debe ser interno (OP_INT) para MUNICIPIO");
            }
        } else {
            if (!"OP_EXT".equalsIgnoreCase(operador.rol())) {
                throw new AccessDeniedException("Operador debe ser externo (OP_EXT) para la entidad");
            }
        }

        // 3. Crear el registro de asignacion
        DenunciaAsignacionEntity asignacion = DenunciaAsignacionEntity.builder()
                .denuncia(denunciaEncontrada)
                .operadorAnteriorId(null)
                .operadorNuevoId(asignarOperadorADenuncia.operadorId())
                .asignadoPorId(actorId)
                .ocurridoEn(Instant.now())
                .build();

        //4. Guardar el registro de asignacion
        EstadoDenunciaEnum estadoAnterior = denunciaEncontrada.getEstadoDenunciaEnum();
        denunciaEncontrada.setEstadoDenunciaEnum(EstadoDenunciaEnum.ASIGNADA);
        registrarCambioEstado(denunciaEncontrada, estadoAnterior, EstadoDenunciaEnum.ASIGNADA, actorId);

        //5. Guardar la denuncia actualizada y la asignacion
        denunciaEncontrada.setOperadorId(asignarOperadorADenuncia.operadorId());
        denunciaRepository.save(denunciaEncontrada);

        //6. Guardar la asignacion
        denunciaAsignacionRepository.save(asignacion);

    }

    @Override
    public void iniciarProcesoDenunciaOperadores(Long denunciaId, Long actorId) {
        //1. Buscar la denuncia
        DenunciaEntity denunciaEncontrada = denunciaRepository.findById(denunciaId)
                .orElseThrow(() -> new DenunciaNotFoundException(denunciaId));
        validarNoRechazada(denunciaEncontrada);

        if (denunciaEncontrada.getOperadorId() == null || !denunciaEncontrada.getOperadorId().equals(actorId)) {
            throw new AccessDeniedException("Denuncia no asignada a este operador");
        }

        //2. Validar que la denuncia este en estado ASIGNADA
        if (denunciaEncontrada.getEstadoDenunciaEnum() != EstadoDenunciaEnum.ASIGNADA || denunciaEncontrada.getEstadoDenunciaEnum() == EstadoDenunciaEnum.EN_PROCESO) {
            throw new DenunciaEstadoInvalidoException(denunciaId,
                    EstadoDenunciaEnum.ASIGNADA,
                    denunciaEncontrada.getEstadoDenunciaEnum());
        }

        //3. Actualizar el estado de la denuncia a EN_PROCESO
        EstadoDenunciaEnum estadoAnterior = denunciaEncontrada.getEstadoDenunciaEnum();
        denunciaEncontrada.setEstadoDenunciaEnum(EstadoDenunciaEnum.EN_PROCESO);
        registrarCambioEstado(denunciaEncontrada, estadoAnterior, EstadoDenunciaEnum.EN_PROCESO, actorId);

        //4. Guardar la denuncia actualizada
        denunciaRepository.save(denunciaEncontrada);
    }

    @Override
    @Transactional
    public void resolverDenunciaOperador(Long denunciaId, MarcarResolucionRequest marcarResolucionDenuncia, Long actorId) {

        //1. Buscar la denuncia
        DenunciaEntity denunciaEncontrada = denunciaRepository.findById(denunciaId)
                .orElseThrow(() -> new DenunciaNotFoundException(denunciaId));
        validarNoRechazada(denunciaEncontrada);

        //2. Validar que la denuncia este en estado EN_PROCESO
        if (denunciaEncontrada.getEstadoDenunciaEnum() != EstadoDenunciaEnum.EN_PROCESO) {
            throw new DenunciaEstadoInvalidoException(denunciaId,
                    EstadoDenunciaEnum.EN_PROCESO,
                    denunciaEncontrada.getEstadoDenunciaEnum());
        }

        //2b. Validar que el operador autenticado sea el asignado
        if (denunciaEncontrada.getOperadorId() == null || !denunciaEncontrada.getOperadorId().equals(actorId)) {
            throw new AccessDeniedException("Denuncia no asignada a este operador");
        }

        //3. Validar que si exista evidencia
        if (marcarResolucionDenuncia.evidenciasIds() == null || marcarResolucionDenuncia.evidenciasIds().isEmpty()) {
            throw new EvidenciasRequeridasException(denunciaId);
        }

        //4. Crear el registro de resolucion
        DenunciaResolucionEntity resolucion = DenunciaResolucionEntity.builder()
                .denuncia(denunciaEncontrada)
                .comentarioResolucion(marcarResolucionDenuncia.comentarioResolucion())
                .resueltoPorId(actorId)
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
                    .usuarioId(actorId)
                    .build()
            );
        } catch (Exception e) {
            throw new EvidenciasVinculacionException("Error al vincular evidencias de resolucion: " + e.getMessage());
        }

        //8. Actualizar el estado de la denuncia a EN_VALIDACION
        EstadoDenunciaEnum estadoAnterior = denunciaEncontrada.getEstadoDenunciaEnum();
        denunciaEncontrada.setEstadoDenunciaEnum(EstadoDenunciaEnum.EN_VALIDACION);

        registrarCambioEstado(denunciaEncontrada, estadoAnterior, EstadoDenunciaEnum.EN_VALIDACION, actorId);
        denunciaRepository.save(denunciaEncontrada);

    }

    @Override
    @Transactional
    public void validarDenunciaJefe(Long denunciaId,
                                    ValidarSolucionRequest validarSolucionDenuncia,
                                    Long actorId,
                                    String rol,
                                    String entidad) {

        //1. Buscar la denuncia
        DenunciaEntity denunciaEncontrada = denunciaRepository.findById(denunciaId)
                .orElseThrow(() -> new DenunciaNotFoundException(denunciaId));
        validarNoRechazada(denunciaEncontrada);

        validarAccesoDeJefe(denunciaEncontrada, rol, entidad);

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
            registrarCambioEstado(denunciaEncontrada, estadoAnterior, EstadoDenunciaEnum.RESUELTA, actorId);
            denunciaRepository.save(denunciaEncontrada);
            // Crear el registro de validacion
            DenunciaValidacionEntity validacion = DenunciaValidacionEntity.builder()
                    .denuncia(denunciaEncontrada)
                    .aprobada(true)
                    .comentarioObservacion(validarSolucionDenuncia.comentarioObservacion())
                    .validadoPorId(actorId)
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
            registrarCambioEstado(denunciaEncontrada, estadoAnterior, EstadoDenunciaEnum.EN_PROCESO, actorId);
            denunciaRepository.save(denunciaEncontrada);
            // Crear el registro de validacion
            DenunciaValidacionEntity validacion = DenunciaValidacionEntity.builder()
                    .denuncia(denunciaEncontrada)
                    .aprobada(false)
                    .comentarioObservacion(validarSolucionDenuncia.comentarioObservacion())
                    .validadoPorId(actorId)
                    .ocurridoEn(Instant.now())
                    .build();
            // Guardar el registro de validacion
            denunciaValidacionRepository.save(validacion);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DenunciaEstadoHistorialResponse historialDenuncia(Long denunciaId, Long actorId, String rol, String entidad) {
        DenunciaEntity denunciaEncontrada = denunciaRepository.findById(denunciaId)
                .orElseThrow(() -> new DenunciaNotFoundException(denunciaId));
        validarAccesoADenuncia(denunciaEncontrada, actorId, rol, entidad);
        List<DenunciaEstadoHistorialEntity> historialEntities =
                denunciaEstadoHistorialRepository.findByDenunciaIdOrderByOcurridoEnAsc(denunciaId);
        return denunciaMapper.toDenunciaEstadoHistorialResponseDTO(denunciaEncontrada, historialEntities);
    }

    @Override
    @Transactional
    public void rechazarDenuncia(Long denunciaId,
                                 RechazarDenunciaRequest request,
                                 Long actorId,
                                 String rol) {
        if (rol == null || (!"SUPERVISOR".equalsIgnoreCase(rol) && !"ADMIN".equalsIgnoreCase(rol))) {
            throw new AccessDeniedException("Solo supervisor o admin puede rechazar denuncias");
        }
        DenunciaEntity denunciaEncontrada = denunciaRepository.findById(denunciaId)
                .orElseThrow(() -> new DenunciaNotFoundException(denunciaId));

        if (denunciaEncontrada.getEstadoDenunciaEnum() == EstadoDenunciaEnum.RESUELTA ||
                denunciaEncontrada.getEstadoDenunciaEnum() == EstadoDenunciaEnum.RECHAZADA) {
            throw new AccessDeniedException("Denuncia no puede ser rechazada en estado " + denunciaEncontrada.getEstadoDenunciaEnum());
        }

        EstadoDenunciaEnum estadoAnterior = denunciaEncontrada.getEstadoDenunciaEnum();
        denunciaEncontrada.setEstadoDenunciaEnum(EstadoDenunciaEnum.RECHAZADA);
        denunciaEncontrada.setComentarioObservacion(request.motivo());
        denunciaEncontrada.setEntidadResponsable(null);
        denunciaEncontrada.setOperadorId(null);
        denunciaEncontrada.setJefeId(null);

        registrarCambioEstado(denunciaEncontrada, estadoAnterior, EstadoDenunciaEnum.RECHAZADA, actorId);
        denunciaRepository.save(denunciaEncontrada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DenunciaListadoResponse> listarDenuncias(Long actorId, String rol, String entidad) {
        List<DenunciaEntity> denuncias;
        String rolUpper = rol != null ? rol.toUpperCase() : "";
        switch (rolUpper) {
            case "SUPERVISOR":
            case "ADMIN":
                denuncias = denunciaRepository.findAll();
                break;
            case "JEFE_OP_INT":
            case "JEFE_OP_EXT":
                validarEntidadPresente(entidad);
                denuncias = denunciaRepository.findByEntidadResponsable(EntidadResponsableEnum.valueOf(entidad.toUpperCase()));
                break;
            case "OP_INT":
            case "OP_EXT":
                denuncias = denunciaRepository.findByOperadorId(actorId);
                break;
            case "CIUDADANO":
                denuncias = denunciaRepository.findByCiudadanoId(actorId);
                break;
            default:
                throw new AccessDeniedException("Rol no autorizado");
        }
        return denuncias.stream()
                .map(d -> new DenunciaListadoResponse(
                        d.getId(),
                        d.getJefeId(),
                        d.getOperadorId(),
                        d.getTitulo(),
                        d.getCiudadanoId(),
                        d.getCreadoEn(),
                        d.getEntidadResponsable(),
                        d.getEstadoDenunciaEnum(),
                        d.getCategoriaDenunciaEnum()
                ))
                .toList();
    }

    private void validarAccesoADenuncia(DenunciaEntity denuncia, Long actorId, String rol, String entidad) {
        if (rol == null) {
            throw new AccessDeniedException("Rol no presente en el token");
        }
        String rolUpper = rol.toUpperCase();
        switch (rolUpper) {
            case "SUPERVISOR":
            case "ADMIN":
                return;
            case "CIUDADANO":
                if (!actorId.equals(denuncia.getCiudadanoId())) {
                    throw new AccessDeniedException("Denuncia no pertenece al ciudadano autenticado");
                }
                return;
            case "JEFE_OP_INT":
            case "JEFE_OP_EXT":
                if (denuncia.getEntidadResponsable() == null || entidad == null) {
                    throw new AccessDeniedException("Entidad no asociada o no presente en el token");
                }
                if (!denuncia.getEntidadResponsable().name().equalsIgnoreCase(entidad)) {
                    throw new AccessDeniedException("Denuncia no pertenece a su entidad");
                }
                return;
            case "OP_INT":
            case "OP_EXT":
                if (denuncia.getOperadorId() == null || !denuncia.getOperadorId().equals(actorId)) {
                    throw new AccessDeniedException("Denuncia no asignada al operador");
                }
                return;
            default:
                throw new AccessDeniedException("Rol no autorizado");
        }
    }

    private void validarAsignador(DenunciaEntity denuncia, String rol, String entidad) {
        if (rol == null) {
            throw new AccessDeniedException("Rol no presente en el token");
        }
        String rolUpper = rol.toUpperCase();
        if ("SUPERVISOR".equals(rolUpper) || "ADMIN".equals(rolUpper)) {
            return;
        }
        if ("JEFE_OP_INT".equals(rolUpper) || "JEFE_OP_EXT".equals(rolUpper)) {
            if (denuncia.getEntidadResponsable() == null || entidad == null) {
                throw new AccessDeniedException("Entidad no asociada o no presente en el token");
            }
            if (!denuncia.getEntidadResponsable().name().equalsIgnoreCase(entidad)) {
                throw new AccessDeniedException("No puede asignar operadores fuera de su entidad");
            }
            return;
        }
        throw new AccessDeniedException("Rol no autorizado para asignar operadores");
    }

    private void validarAccesoDeJefe(DenunciaEntity denuncia, String rol, String entidad) {
        if (rol == null) {
            throw new AccessDeniedException("Rol no presente en el token");
        }
        String rolUpper = rol.toUpperCase();
        if (!"JEFE_OP_INT".equals(rolUpper) && !"JEFE_OP_EXT".equals(rolUpper)) {
            throw new AccessDeniedException("Solo jefes pueden validar denuncias");
        }
        if (denuncia.getEntidadResponsable() == null || entidad == null ||
                !denuncia.getEntidadResponsable().name().equalsIgnoreCase(entidad)) {
            throw new AccessDeniedException("Denuncia no pertenece a su entidad");
        }
    }

    private void validarEntidadPresente(String entidad) {
        if (entidad == null || entidad.isBlank()) {
            throw new AccessDeniedException("Entidad no presente en el token");
        }
    }

    private void validarNoRechazada(DenunciaEntity denuncia) {
        if (denuncia.getEstadoDenunciaEnum() == EstadoDenunciaEnum.RECHAZADA) {
            throw new AccessDeniedException("Denuncia rechazada, no se puede modificar");
        }
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

