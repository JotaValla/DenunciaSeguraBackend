package com.andervalla.msdenuncias.repositories;

import com.andervalla.msdenuncias.models.DenunciaEstadoHistorialEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DenunciaEstadoHistorialRepository extends JpaRepository<DenunciaEstadoHistorialEntity, Long> {


}
