package com.andervalla.msevidencias.services;

import com.andervalla.msevidencias.controllers.dtos.requests.AdjuntarEvidenciaRequest;
import com.andervalla.msevidencias.controllers.dtos.responses.EvidenciaInternaResponse;
import com.andervalla.msevidencias.models.Enums.EntidadTipoEnum;
import com.andervalla.msevidencias.models.Enums.EstadoEvidenciaEnum;
import com.andervalla.msevidencias.models.EvidenciaEntity;
import com.andervalla.msevidencias.repositories.EvidenciaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EvidenciaServiceImpl implements IEvidenciaService{

    private final EvidenciaRepository evidenciaRepository;
    private final SupabaseStorageService supabaseStorageService;

    public EvidenciaServiceImpl(EvidenciaRepository evidenciaRepository, SupabaseStorageService supabaseStorageService) {
        this.evidenciaRepository = evidenciaRepository;
        this.supabaseStorageService = supabaseStorageService;
    }


    @Override
    @Transactional
    public void adjuntarEvidenciasAEntidad(AdjuntarEvidenciaRequest request) {
        //1. Buscar las evidencias por los IDs
        List<EvidenciaEntity> evidencias = evidenciaRepository.findAllById(request.evidenciasIds());

        //2. Validar que se encontraron todas las evidencias
        if (evidencias.size() != request.evidenciasIds().size()) {
            throw new RuntimeException("Una o más evidencias no fueron encontradas");
        }

        //3. Validar estado y propiedad
        for (EvidenciaEntity ev : evidencias) {
            // Solo adjuntar si el estado es "DISPONIBLE"
            if (ev.getEstado() != EstadoEvidenciaEnum.DISPONIBLE){
                throw new RuntimeException("La evidencia con ID " + ev.getId() + " no está en estado DISPONIBLE");
            }
            // Validar que no pertenezca ya a otra entidad distinta
            if (ev.getEntidadId() != null && !ev.getEntidadId().equals(request.entidadId())) {
                throw new RuntimeException("La evidencia con ID " + ev.getId() + " ya está asociada a otra entidad");
            }
            // Vincular
            ev.setEntidadId(request.entidadId());
            ev.setTipoEntidad(request.entidadTipo());
        }
        evidenciaRepository.saveAll(evidencias);
    }

    @Override
    public List<EvidenciaInternaResponse> buscarPorEntidad(EntidadTipoEnum tipo, Long id) {
        // 1. Buscamos los archivos en BD
        List<EvidenciaEntity> entities = evidenciaRepository.findByTipoEntidadAndEntidadId(tipo, id);

        // 2. Por cada archivo, pedimos URL fresca a Supabase
        return entities.stream().map(e -> {
            String urlFirmada = supabaseStorageService.obtenerUrlFirmada(e.getPathStorage());

            return new EvidenciaInternaResponse(
                    e.getId(),
                    urlFirmada, // <--- Aquí va la URL segura con token
                    e.getContentType(),
                    e.getSizeBytes()
            );
        }).toList();
    }
}
