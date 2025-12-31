package com.andervalla.msevidencias.services;

import com.andervalla.msevidencias.controllers.dtos.requests.AdjuntarEvidenciaRequest;
import com.andervalla.msevidencias.controllers.dtos.responses.EvidenciaInternaResponse;
import com.andervalla.msevidencias.exceptions.ResourceNotFoundException;
import com.andervalla.msevidencias.exceptions.ResourceNotValidException;
import com.andervalla.msevidencias.models.Enums.EntidadTipoEnum;
import com.andervalla.msevidencias.models.Enums.EstadoEvidenciaEnum;
import com.andervalla.msevidencias.models.EvidenciaEntity;
import com.andervalla.msevidencias.repositories.EvidenciaRepository;
import com.andervalla.msevidencias.utils.MagicBytesValidator;
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
                    urlFirmada,
                    e.getContentType(),
                    e.getSizeBytes()
            );
        }).toList();
    }

    @Override
    public void confirmarEvidencia(String evidenciaId) {
        // 1. Buscar la evidencia (debe existir y estar en un estado previo a DISPONIBLE)
        EvidenciaEntity evidencia = evidenciaRepository.findById(evidenciaId)
                .orElseThrow(() -> new ResourceNotFoundException("Evidencia no encontrada"));

        // 2. Descargar SOLO los primeros bytes desde Supabase (cabecera)
        byte[] fileHeader = supabaseStorageService.descargarPrimerosBytes(evidencia.getPathStorage(), 20);

        // 3. Validar Magic Bytes
        boolean esValido = MagicBytesValidator.esFormatoValido(fileHeader, evidencia.getContentType());

        if (!esValido) {
            supabaseStorageService.eliminarArchivo(evidencia.getPathStorage());
            evidencia.setEstado(EstadoEvidenciaEnum.BLOQUEADA);
            evidenciaRepository.save(evidencia);
            throw new ResourceNotValidException("ALERTA DE SEGURIDAD: El archivo no coincide con su extensión.");
        }

        // 4. Si pasa la prueba, lo hacemos oficial
        evidencia.setEstado(EstadoEvidenciaEnum.DISPONIBLE);
        evidenciaRepository.save(evidencia);
    }

    @Override
    @Transactional
    public EvidenciaInternaResponse iniciarCarga(String filename, String contentType, Long size) {
        EvidenciaEntity evidencia = new EvidenciaEntity();

        // 1. Datos básicos
        evidencia.setEstado(EstadoEvidenciaEnum.PENDIENTE_SUBIDA);
        evidencia.setContentType(contentType);
        evidencia.setSizeBytes(size);
        evidencia.setNombreArchivo(filename);

        // 2. Ruta storage
        String pathStorage = java.util.UUID.randomUUID() + "_" + filename;
        evidencia.setPathStorage(pathStorage);

        // 3. Fechas (si no usaste @UpdateTimestamp)
        evidencia.setActualizadoEn(java.time.Instant.now());

        // 4. Usuario Creador
        // Como la columna es obligatoria, ponemos un ID fijo (ej. 1L o 999L)
        evidencia.setUsuarioCreadorId(1L);

        evidencia = evidenciaRepository.save(evidencia);

        // Generar URL de subida
        String urlSubida = supabaseStorageService.obtenerUrlFirmadaParaSubida(evidencia.getPathStorage());

        return new EvidenciaInternaResponse(
                evidencia.getId(),
                urlSubida,
                contentType,
                size
        );
    }

}
