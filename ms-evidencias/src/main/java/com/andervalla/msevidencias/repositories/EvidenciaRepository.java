package com.andervalla.msevidencias.repositories;

import com.andervalla.msevidencias.models.Enums.EntidadTipoEnum;
import com.andervalla.msevidencias.models.EvidenciaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvidenciaRepository extends  JpaRepository<EvidenciaEntity, String> {
    List<EvidenciaEntity> findByTipoEntidadAndEntidadId (EntidadTipoEnum tipoContenido, Long entidadId);
}
