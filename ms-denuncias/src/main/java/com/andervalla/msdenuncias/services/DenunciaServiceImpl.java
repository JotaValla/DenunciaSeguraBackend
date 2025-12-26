package com.andervalla.msdenuncias.services;

import com.andervalla.msdenuncias.controllers.dtos.requests.AsignarOperadorRequest;
import com.andervalla.msdenuncias.controllers.dtos.requests.CrearDenunciaRequest;
import com.andervalla.msdenuncias.controllers.dtos.requests.MarcarResolucionRequest;
import com.andervalla.msdenuncias.controllers.dtos.requests.ValidarSolucionRequest;
import com.andervalla.msdenuncias.controllers.dtos.responses.DenunciaEstadoHistorialResponse;
import com.andervalla.msdenuncias.controllers.dtos.responses.DenunciaResponse;
import com.andervalla.msdenuncias.controllers.dtos.responses.DenunciaResumenResponse;
import com.andervalla.msdenuncias.models.DenunciaEntity;
import com.andervalla.msdenuncias.models.DenunciaEstadoHistorialEntity;
import com.andervalla.msdenuncias.models.enums.EstadoDenunciaEnum;
import com.andervalla.msdenuncias.repositories.*;
import com.andervalla.msdenuncias.services.mappers.DenunciaMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DenunciaServiceImpl implements IDenunciaService {

    private final DenunciaRepository denunciaRepository;
    private final DenunciaEstadoHistorialRepository denunciaEstadoHistorialRepository;
    private final DenunciaAsignacionRepository denunciaAsignacionRepository;
    private final DenunciaResolucionRepository denunciaResolucionRepository;
    private final DenunciaValidacionRepository denunciaValidacionRepository;
    private final DenunciaMapper denunciaMapper;

    public DenunciaServiceImpl(
            DenunciaRepository denunciaRepository,
            DenunciaEstadoHistorialRepository denunciaEstadoHistorialRepository,
            DenunciaAsignacionRepository denunciaAsignacionRepository,
            DenunciaResolucionRepository denunciaResolucionRepository,
            DenunciaValidacionRepository denunciaValidacionRepository,
            DenunciaMapper denunciaMapper
    ) {
        this.denunciaRepository = denunciaRepository;
        this.denunciaEstadoHistorialRepository = denunciaEstadoHistorialRepository;
        this.denunciaAsignacionRepository = denunciaAsignacionRepository;
        this.denunciaResolucionRepository = denunciaResolucionRepository;
        this.denunciaValidacionRepository = denunciaValidacionRepository;
        this.denunciaMapper = denunciaMapper;
    }

    @Override
    @Transactional
    public DenunciaResumenResponse crearDenuncia(CrearDenunciaRequest denunciaReq) {
        DenunciaEntity denunciaEntity = DenunciaEntity.builder()
                .titulo(denunciaReq.titulo())
                .descripcion(denunciaReq.descripcion())
                .categoriaDenunciaEnum(denunciaReq.categoriaDenuncia())
                .latitud(denunciaReq.latitud())
                .longitud(denunciaReq.longitud())
                .nivelAnonimatoEnum(denunciaReq.nivelAnonimato())
                .ciudadanoId(denunciaReq.ciudadanoId())
                .estadoDenunciaEnum(EstadoDenunciaEnum.RECIBIDA)
                .build();

        DenunciaEntity denunciaGuardada = denunciaRepository.save(denunciaEntity);
        registrarCambioEstado(denunciaGuardada, null, denunciaGuardada.getEstadoDenunciaEnum(), denunciaGuardada.getCiudadanoId());
        return new DenunciaResumenResponse(denunciaGuardada.getId(), "Denuncia creada");
    }

    @Override
    @Transactional(readOnly = true)
    public DenunciaResponse obtenerDenuncia(Long denunciaId) {
        DenunciaEntity denunciaEncontrada = denunciaRepository.findById(denunciaId).orElseThrow();
        return denunciaMapper.toDenunciaResponseDTO(denunciaEncontrada);
    }

    @Override
    @Transactional
    public Void asignarDenunciaOperador(Long denunciaId, AsignarOperadorRequest asignarOperadorADenuncia) {
        DenunciaEntity denunciaEncontrada = denunciaRepository.findById(denunciaId).orElseThrow();

        if (!(denunciaEncontrada.getEstadoDenunciaEnum().equals(EstadoDenunciaEnum.RECIBIDA))) {
            throw new RuntimeException("Operador no encontrado"); //TODO: Cambiar esto
        }

        return null;

    }

    @Override
    public Void resolverDenunciaOperador(Long denunciaId, MarcarResolucionRequest marcarResolucionDenuncia) {
        return null;
    }

    @Override
    public Void validarDenunciaJefe(Long denunciaId, ValidarSolucionRequest validarSolucionDenuncia) {
        return null;
    }

    @Override
    public DenunciaEstadoHistorialResponse historialDenuncia(Long denunciaId) {
        return null;
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
        denunciaEstadoHistorialRepository.save(historialDenuncia);
    }
    
}
