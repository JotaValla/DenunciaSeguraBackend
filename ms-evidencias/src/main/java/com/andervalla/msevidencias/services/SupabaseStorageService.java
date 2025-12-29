package com.andervalla.msevidencias.services;

import com.andervalla.msevidencias.controllers.dtos.SupabaseSignResponse;
import com.andervalla.msevidencias.exceptions.StorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class SupabaseStorageService {

    private final RestClient restClient;
    private final String bucketName;
    private final String supabaseUrl;

    public SupabaseStorageService(@Value("${app.supabase.url}") String supabaseUrl,
                                  @Value("${app.supabase.key}") String supabaseKey,
                                  @Value("${app.supabase.bucket}") String bucketName) {

        this.supabaseUrl = supabaseUrl.endsWith("/") ? supabaseUrl.substring(0, supabaseUrl.length() - 1) : supabaseUrl;
        this.bucketName = bucketName;

        String key = supabaseKey == null ? "" : supabaseKey.trim();

        this.restClient = RestClient.builder()
                .baseUrl(this.supabaseUrl)
                .defaultHeader("Authorization", "Bearer " + key)
                .defaultHeader("apikey", key)
                .build();
    }

    public String obtenerUrlFirmada(String pathArchivo) {
        try {
            String cleanPath = pathArchivo == null ? "" : pathArchivo.trim();
            if (cleanPath.startsWith("/")) cleanPath = cleanPath.substring(1);

            SupabaseSignResponse response = restClient.post()
                    .uri("/storage/v1/object/sign/{bucket}/{path}", bucketName, cleanPath)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("expiresIn", 3600))
                    .retrieve()
                    .body(SupabaseSignResponse.class);

            if (response == null || response.signedUrl() == null || response.signedUrl().isBlank()) {
                throw new StorageException("Supabase no devolvió una URL válida (revisa el JSON real de respuesta)");
            }

            String signed = response.signedUrl();

            if (signed.startsWith("http")) return signed;
            if (signed.startsWith("/storage/v1")) return this.supabaseUrl + signed;
            if (signed.startsWith("/object")) return this.supabaseUrl + "/storage/v1" + signed;

            return this.supabaseUrl + "/storage/v1/" + signed;

        } catch (Exception e) {
            throw new StorageException("Error al firmar URL en Supabase: " + e.getMessage());
        }
    }
}
