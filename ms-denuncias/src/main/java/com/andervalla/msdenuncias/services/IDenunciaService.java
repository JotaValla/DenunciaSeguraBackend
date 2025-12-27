package com.andervalla.msdenuncias.services;

import com.andervalla.msdenuncias.controllers.dtos.requests.AsignarOperadorRequest;
import com.andervalla.msdenuncias.controllers.dtos.requests.CrearDenunciaRequest;
import com.andervalla.msdenuncias.controllers.dtos.requests.MarcarResolucionRequest;
import com.andervalla.msdenuncias.controllers.dtos.requests.ValidarSolucionRequest;
import com.andervalla.msdenuncias.controllers.dtos.responses.DenunciaEstadoHistorialResponse;
import com.andervalla.msdenuncias.controllers.dtos.responses.DenunciaResponse;
import com.andervalla.msdenuncias.controllers.dtos.responses.DenunciaResumenResponse;
import com.andervalla.msdenuncias.models.DenunciaEntity;
import com.andervalla.msdenuncias.models.enums.EntidadResponsableEnum;

import java.util.List;

public interface IDenunciaService {

    DenunciaResumenResponse crearDenuncia(CrearDenunciaRequest denunciaReq);

    DenunciaResponse obtenerDenuncia(Long denunciaId);

    void asignarDenunciaOperador(Long denunciaId, AsignarOperadorRequest asignarOperadorADenuncia);

    void asignarDenunciaEntidadResponsableSupervisor(Long denunciaId, EntidadResponsableEnum entidadResponsableEnum);

    void iniciarProcesoDenunciaOperadores(Long denunciaId, Long operadorId);

    void resolverDenunciaOperador(Long denunciaId, MarcarResolucionRequest marcarResolucionDenuncia);

    void validarDenunciaJefe(Long denunciaId, ValidarSolucionRequest validarSolucionDenuncia);

    DenunciaEstadoHistorialResponse historialDenuncia (Long  denunciaId);

}
