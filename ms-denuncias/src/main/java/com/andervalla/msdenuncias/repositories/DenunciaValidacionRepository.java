package com.andervalla.msdenuncias.repositories;

import com.andervalla.msdenuncias.models.DenunciaValidacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DenunciaValidacionRepository extends JpaRepository<DenunciaValidacionEntity, Long> {
}
