package com.andervalla.msdenuncias.services;

import com.andervalla.msdenuncias.controllers.dtos.requests.AsignarOperadorRequest;
import com.andervalla.msdenuncias.controllers.dtos.requests.CrearDenunciaRequest;
import com.andervalla.msdenuncias.controllers.dtos.requests.MarcarResolucionRequest;
import com.andervalla.msdenuncias.controllers.dtos.requests.ValidarSolucionRequest;
import com.andervalla.msdenuncias.controllers.dtos.responses.DenunciaEstadoHistorialResponse;
import com.andervalla.msdenuncias.controllers.dtos.responses.DenunciaResponse;
import com.andervalla.msdenuncias.controllers.dtos.responses.DenunciaResumenResponse;
import com.andervalla.msdenuncias.models.DenunciaEntity;

import java.util.List;

public interface IDenunciaService {

    DenunciaResumenResponse crearDenuncia(CrearDenunciaRequest denunciaReq);

    DenunciaResponse obtenerDenuncia(Long denunciaId);

    Void asignarDenunciaOperador(Long denunciaId, AsignarOperadorRequest asignarOperadorADenuncia);

    Void resolverDenunciaOperador(Long denunciaId, MarcarResolucionRequest marcarResolucionDenuncia);

    Void validarDenunciaJefe(Long denunciaId, ValidarSolucionRequest validarSolucionDenuncia);

    DenunciaEstadoHistorialResponse historialDenuncia (Long  denunciaId);

}
