package com.andervalla.msevidencias.services;

import com.andervalla.msevidencias.controllers.dtos.requests.AdjuntarEvidenciaRequest;
import com.andervalla.msevidencias.controllers.dtos.responses.EvidenciaInternaResponse;
import com.andervalla.msevidencias.models.Enums.EntidadTipoEnum;

import java.util.List;

public interface IEvidenciaService {

    void adjuntarEvidenciasAEntidad(AdjuntarEvidenciaRequest request);
    List<EvidenciaInternaResponse> buscarPorEntidad(EntidadTipoEnum tipo, Long id);
    void confirmarEvidencia(String evidenciaId);
    EvidenciaInternaResponse iniciarCarga(String filename, String contentType, Long sizeBytes);

}
