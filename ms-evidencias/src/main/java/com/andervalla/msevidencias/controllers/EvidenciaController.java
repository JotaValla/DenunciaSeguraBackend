package com.andervalla.msevidencias.controllers;

import com.andervalla.msevidencias.controllers.dtos.requests.AdjuntarEvidenciaRequest;
import com.andervalla.msevidencias.controllers.dtos.responses.EvidenciaInternaResponse;
import com.andervalla.msevidencias.models.Enums.EntidadTipoEnum;
import com.andervalla.msevidencias.services.IEvidenciaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/interno")
@RequiredArgsConstructor
public class EvidenciaController {
    private final IEvidenciaService evidenciaService;

    @PostMapping("/adjuntar")
    @PreAuthorize("hasAnyRole('CIUDADANO','OP_INT','OP_EXT','JEFE_OP_INT','JEFE_OP_EXT','SUPERVISOR','ADMIN')")
    public ResponseEntity<Void> adjuntarEvidencias(@RequestBody @Valid AdjuntarEvidenciaRequest request) {
        evidenciaService.adjuntarEvidenciasAEntidad(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/entidades/{tipo}/{id}/evidencias")
    @PreAuthorize("hasAnyRole('CIUDADANO','OP_INT','OP_EXT','JEFE_OP_INT','JEFE_OP_EXT','SUPERVISOR','ADMIN')")
    public ResponseEntity<List<EvidenciaInternaResponse>> obtenerEvidencias(
            @PathVariable EntidadTipoEnum tipo,
            @PathVariable Long id) {
        return ResponseEntity.ok(evidenciaService.buscarPorEntidad(tipo, id));
    }

    @PostMapping("/{id}/confirmar")
    @PreAuthorize("hasAnyRole('CIUDADANO','OP_INT','OP_EXT')")
    public ResponseEntity<Void> confirmarCarga(@PathVariable String id) {
        evidenciaService.confirmarEvidencia(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/uploads")
    @PreAuthorize("hasAnyRole('CIUDADANO','OP_INT','OP_EXT')")
    public ResponseEntity<EvidenciaInternaResponse> crearIntencionDeCarga(
            @RequestParam String filename,
            @RequestParam String contentType,
            @RequestParam Long size) {

        return ResponseEntity.ok(evidenciaService.iniciarCarga(filename, contentType, size));
    }


}
