package com.andervalla.msdenuncias.repositories;

import com.andervalla.msdenuncias.models.DenunciaEntity;
import com.andervalla.msdenuncias.models.enums.EntidadResponsableEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DenunciaRepository extends JpaRepository<DenunciaEntity, Long> {
    List<DenunciaEntity> findByCiudadanoId(Long ciudadanoId);
    List<DenunciaEntity> findByHashIdentidad(String hashIdentidad);
    List<DenunciaEntity> findByOperadorId(Long operadorId);
    List<DenunciaEntity> findByEntidadResponsable(EntidadResponsableEnum entidadResponsable);
}
