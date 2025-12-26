package com.andervalla.msdenuncias.repositories;

import com.andervalla.msdenuncias.models.DenunciaAsignacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DenunciaAsignacionRepository extends JpaRepository<DenunciaAsignacionEntity, Long> {
}
