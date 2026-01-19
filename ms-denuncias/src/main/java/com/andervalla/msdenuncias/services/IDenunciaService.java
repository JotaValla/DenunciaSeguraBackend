package com.andervalla.msdenuncias.services;

import com.andervalla.msdenuncias.controllers.dtos.requests.AsignarOperadorRequest;
import com.andervalla.msdenuncias.controllers.dtos.requests.CrearDenunciaRequest;
import com.andervalla.msdenuncias.controllers.dtos.requests.MarcarResolucionRequest;
import com.andervalla.msdenuncias.controllers.dtos.requests.ValidarSolucionRequest;
import com.andervalla.msdenuncias.controllers.dtos.responses.DenunciaListadoResponse;
import com.andervalla.msdenuncias.controllers.dtos.responses.DenunciaEstadoHistorialResponse;
import com.andervalla.msdenuncias.controllers.dtos.responses.DenunciaResponse;
import com.andervalla.msdenuncias.controllers.dtos.responses.DenunciaResumenResponse;
import com.andervalla.msdenuncias.models.enums.EntidadResponsableEnum;

public interface IDenunciaService {

    DenunciaResumenResponse crearDenuncia(CrearDenunciaRequest denunciaReq, Long actorId);

    DenunciaResponse obtenerDenuncia(Long denunciaId, Long actorId, String rol, String entidad);
    java.util.List<DenunciaListadoResponse> listarDenuncias(Long actorId, String rol, String entidad);

    void asignarDenunciaOperador(Long denunciaId, AsignarOperadorRequest asignarOperadorADenuncia, Long actorId, String rol, String entidad);

    void asignarDenunciaEntidadResponsableSupervisor(Long denunciaId, EntidadResponsableEnum entidadResponsableEnum, Long actorId, String rol);

    void iniciarProcesoDenunciaOperadores(Long denunciaId, Long actorId);

    void resolverDenunciaOperador(Long denunciaId, MarcarResolucionRequest marcarResolucionDenuncia, Long actorId);

    void validarDenunciaJefe(Long denunciaId, ValidarSolucionRequest validarSolucionDenuncia, Long actorId, String rol, String entidad);

    void rechazarDenuncia(Long denunciaId, com.andervalla.msdenuncias.controllers.dtos.requests.RechazarDenunciaRequest request, Long actorId, String rol);

    DenunciaEstadoHistorialResponse historialDenuncia(Long denunciaId, Long actorId, String rol, String entidad);

}
