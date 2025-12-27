package com.andervalla.msdenuncias.controllers;

import com.andervalla.msdenuncias.controllers.dtos.requests.AsignarOperadorRequest;
import com.andervalla.msdenuncias.controllers.dtos.requests.CrearDenunciaRequest;
import com.andervalla.msdenuncias.controllers.dtos.requests.MarcarResolucionRequest;
import com.andervalla.msdenuncias.controllers.dtos.requests.ValidarSolucionRequest;
import com.andervalla.msdenuncias.controllers.dtos.responses.DenunciaEstadoHistorialResponse;
import com.andervalla.msdenuncias.controllers.dtos.responses.DenunciaResponse;
import com.andervalla.msdenuncias.controllers.dtos.responses.DenunciaResumenResponse;
import com.andervalla.msdenuncias.services.IDenunciaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/denuncias")
public class DenunciaController {

    private final IDenunciaService denunciaService;

    public DenunciaController(IDenunciaService denunciaService) {
        this.denunciaService = denunciaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DenunciaResumenResponse crearDenuncia(@Valid @RequestBody CrearDenunciaRequest denunciaRequest){
        return denunciaService.crearDenuncia(denunciaRequest);
    }

    @GetMapping("/{denunciaId}")
    public DenunciaResponse obtenerDenunciaPorId(@PathVariable Long denunciaId){
        return denunciaService.obtenerDenuncia(denunciaId);
    }

    @PostMapping("/{denunciaId}/asignacion-operador")
    @ResponseStatus(HttpStatus.OK)
    public void asignarOperadorAdenuncia(
            @PathVariable Long denunciaId,
            @Valid @RequestBody AsignarOperadorRequest operadorRequest){
        denunciaService.asignarDenunciaOperador(denunciaId, operadorRequest);
    }

    @PostMapping("/{denunciaId}/iniciar-proceso")
    @ResponseStatus(HttpStatus.OK)
    public void iniciarProcesoDenunciaOperadores(
            @PathVariable Long denunciaId,
            @RequestParam Long operadorId){
        denunciaService.iniciarProcesoDenunciaOperadores(denunciaId, operadorId);
    }

    @PostMapping("/{denunciaId}/resolucion")
    @ResponseStatus(HttpStatus.OK)
    public void resolverDenuncia(@PathVariable Long denunciaId,
                                 @Valid @RequestBody MarcarResolucionRequest resolucionRequest){
        denunciaService.resolverDenunciaOperador(denunciaId, resolucionRequest);
    }

    @PostMapping("/{denunciaId}/validacion")
    @ResponseStatus(HttpStatus.OK)
    public void validarSolucionDenuncia(
            @PathVariable Long denunciaId,
            @Valid @RequestBody ValidarSolucionRequest validarSolucionRequest){
        denunciaService.validarDenunciaJefe(denunciaId, validarSolucionRequest);
    }

    @GetMapping("/{denunciaId}/historial-estados")
    public DenunciaEstadoHistorialResponse denunciaHistorialEstados(@PathVariable Long denunciaId){
        return denunciaService.historialDenuncia(denunciaId);
    }
}
