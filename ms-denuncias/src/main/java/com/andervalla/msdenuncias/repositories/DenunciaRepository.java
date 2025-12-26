package com.andervalla.msdenuncias.repositories;

import com.andervalla.msdenuncias.models.DenunciaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DenunciaRepository extends JpaRepository<DenunciaEntity, Long> {

}
