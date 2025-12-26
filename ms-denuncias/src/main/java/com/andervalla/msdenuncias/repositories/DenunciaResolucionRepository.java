package com.andervalla.msdenuncias.repositories;

import com.andervalla.msdenuncias.models.DenunciaResolucionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DenunciaResolucionRepository extends JpaRepository<DenunciaResolucionEntity, Long> {
}
