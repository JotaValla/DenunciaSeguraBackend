package com.andervalla.msdenuncias.clients;

import com.andervalla.msdenuncias.clients.dtos.AdjuntarEvidenciaRequest;
import com.andervalla.msdenuncias.clients.dtos.EvidenciaDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "ms-evidencias", url = "${CLIENTS_EVIDENCIAS_URI:http://localhost:9094}")
@Component
public interface EvidenciasClient {

    @PostMapping("/interno/adjuntar")
    void adjuntarEvidencias(@RequestBody AdjuntarEvidenciaRequest request);

    @GetMapping("/interno/entidades/{tipo}/{id}/evidencias")
    List<EvidenciaDTO> obtenerEvidencias(@PathVariable String tipo, @PathVariable Long id);
}
