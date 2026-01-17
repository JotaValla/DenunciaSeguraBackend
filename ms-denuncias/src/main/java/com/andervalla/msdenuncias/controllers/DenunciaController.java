package com.andervalla.msdenuncias.controllers;

import com.andervalla.msdenuncias.controllers.dtos.requests.AsignarOperadorRequest;
import com.andervalla.msdenuncias.controllers.dtos.requests.CrearDenunciaRequest;
import com.andervalla.msdenuncias.controllers.dtos.requests.MarcarResolucionRequest;
import com.andervalla.msdenuncias.controllers.dtos.requests.ValidarSolucionRequest;
import com.andervalla.msdenuncias.controllers.dtos.responses.DenunciaListadoResponse;
import com.andervalla.msdenuncias.controllers.dtos.responses.DenunciaEstadoHistorialResponse;
import com.andervalla.msdenuncias.controllers.dtos.responses.DenunciaResponse;
import com.andervalla.msdenuncias.controllers.dtos.responses.DenunciaResumenResponse;
import com.andervalla.msdenuncias.models.enums.EntidadResponsableEnum;
import com.andervalla.msdenuncias.services.IDenunciaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
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
    @PreAuthorize("hasRole('CIUDADANO')")
    public DenunciaResumenResponse crearDenuncia(@Valid @RequestBody CrearDenunciaRequest denunciaRequest,
                                                 JwtAuthenticationToken authentication){
        return denunciaService.crearDenuncia(denunciaRequest, getUsuarioId(authentication));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CIUDADANO','SUPERVISOR','ADMIN','JEFE_OP_INT','JEFE_OP_EXT','OP_INT','OP_EXT')")
    public java.util.List<DenunciaListadoResponse> listarDenuncias(JwtAuthenticationToken authentication) {
        return denunciaService.listarDenuncias(getUsuarioId(authentication), getRol(authentication), getEntidad(authentication));
    }

    @GetMapping("/{denunciaId}")
    @PreAuthorize("hasAnyRole('CIUDADANO','SUPERVISOR','ADMIN','JEFE_OP_INT','JEFE_OP_EXT','OP_INT','OP_EXT')")
    public DenunciaResponse obtenerDenunciaPorId(@PathVariable Long denunciaId,
                                                 JwtAuthenticationToken authentication){
        return denunciaService.obtenerDenuncia(denunciaId, getUsuarioId(authentication), getRol(authentication), getEntidad(authentication));
    }

    @PostMapping("/{denunciaId}/asignacion-entidad")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN')")
    public void asignarEntidadADenuncia(@PathVariable Long denunciaId,
                                        @RequestParam String entidad,
                                        JwtAuthenticationToken authentication){
        denunciaService.asignarDenunciaEntidadResponsableSupervisor(denunciaId, EntidadResponsableEnum.valueOf(entidad), getUsuarioId(authentication), getRol(authentication));
    }

    @PostMapping("/{denunciaId}/asignacion-operador")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN','JEFE_OP_INT','JEFE_OP_EXT')")
    public void asignarOperadorAdenuncia(
            @PathVariable Long denunciaId,
            @Valid @RequestBody AsignarOperadorRequest operadorRequest,
            JwtAuthenticationToken authentication){
        denunciaService.asignarDenunciaOperador(denunciaId, operadorRequest, getUsuarioId(authentication), getRol(authentication), getEntidad(authentication));
    }

    @PostMapping("/{denunciaId}/iniciar-proceso")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('OP_INT','OP_EXT')")
    public void iniciarProcesoDenunciaOperadores(@PathVariable Long denunciaId,
                                                 JwtAuthenticationToken authentication){
        denunciaService.iniciarProcesoDenunciaOperadores(denunciaId, getUsuarioId(authentication));
    }

    @PostMapping("/{denunciaId}/resolucion")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('OP_INT','OP_EXT')")
    public void resolverDenuncia(@PathVariable Long denunciaId,
                                 @Valid @RequestBody MarcarResolucionRequest resolucionRequest,
                                 JwtAuthenticationToken authentication){
        denunciaService.resolverDenunciaOperador(denunciaId, resolucionRequest, getUsuarioId(authentication));
    }

    @PostMapping("/{denunciaId}/validacion")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('JEFE_OP_INT','JEFE_OP_EXT')")
    public void validarSolucionDenuncia(
            @PathVariable Long denunciaId,
            @Valid @RequestBody ValidarSolucionRequest validarSolucionRequest,
            JwtAuthenticationToken authentication){
        denunciaService.validarDenunciaJefe(denunciaId, validarSolucionRequest, getUsuarioId(authentication), getRol(authentication), getEntidad(authentication));
    }

    @GetMapping("/{denunciaId}/historial-estados")
    @PreAuthorize("hasAnyRole('CIUDADANO','SUPERVISOR','ADMIN','JEFE_OP_INT','JEFE_OP_EXT','OP_INT','OP_EXT')")
    public DenunciaEstadoHistorialResponse denunciaHistorialEstados(@PathVariable Long denunciaId,
                                                                     JwtAuthenticationToken authentication){
        return denunciaService.historialDenuncia(denunciaId, getUsuarioId(authentication), getRol(authentication), getEntidad(authentication));
    }

    private Long getUsuarioId(JwtAuthenticationToken authentication) {
        return Long.valueOf(authentication.getToken().getSubject());
    }

    private String getRol(JwtAuthenticationToken authentication) {
        Object rol = authentication.getToken().getClaim("rol");
        return rol != null ? rol.toString() : null;
    }

    private String getEntidad(JwtAuthenticationToken authentication) {
        Object entidad = authentication.getToken().getClaim("entidad");
        return entidad != null ? entidad.toString() : null;
    }
}
