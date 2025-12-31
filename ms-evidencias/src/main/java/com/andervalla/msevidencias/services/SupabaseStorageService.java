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
            String cleanPath = limpiarPath(pathArchivo);
            if (cleanPath.startsWith("/")) cleanPath = cleanPath.substring(1);

            SupabaseSignResponse response = restClient.post()
                    .uri("/storage/v1/object/sign/{bucket}/{path}", bucketName, cleanPath)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("expiresIn", 60))
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

    public String obtenerUrlFirmadaParaSubida(String pathArchivo) {
        try {
            String cleanPath = limpiarPath(pathArchivo);

            // 1. Solicitamos el token de subida
            SupabaseSignResponse response = restClient.post()
                    .uri("/storage/v1/object/upload/sign/{bucket}/{path}", bucketName, cleanPath)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("upsert", true))
                    .retrieve()
                    .body(SupabaseSignResponse.class);

            if (response == null || response.token() == null || response.token().isBlank()) {
                throw new StorageException("Supabase no devolvió un token de subida válido.");
            }

            // 2. Construimos la URL final para el PUT
            // Formato: BASE + /storage/v1/object/upload/sign/BUCKET/PATH + ?token=TOKEN
            return String.format("%s/storage/v1/object/upload/sign/%s/%s?token=%s",
                    this.supabaseUrl,
                    this.bucketName,
                    cleanPath,
                    response.token());

        } catch (Exception e) {
            throw new StorageException("Error al generar URL de subida: " + e.getMessage());
        }
    }

    public byte[] descargarPrimerosBytes(String pathArchivo, int cantidadBytes) {
        try {
            // 1. Pedimos permiso: Generamos una URL firmada de DESCARGA (GET) válida por 60 seg.
            String urlDescargaSegura = obtenerUrlFirmada(pathArchivo);

            return RestClient.create().get()
                    .uri(urlDescargaSegura)
                    .header("Range", "bytes=0-" + (cantidadBytes - 1)) // Pedimos solo el pedacito inicial
                    .retrieve()
                    .body(byte[].class);

        } catch (Exception e) {
            throw new StorageException("No se pudieron leer los bytes del archivo: " + e.getMessage());
        }
    }

    private String limpiarPath(String path) {
        if (path == null) return "";
        String trimmed = path.trim();
        return trimmed.startsWith("/") ? trimmed.substring(1) : trimmed;
    }

    public void eliminarArchivo(String pathArchivo) {
        try {
            String cleanPath = pathArchivo.startsWith("/") ? pathArchivo.substring(1) : pathArchivo;

            // Llamada DELETE a Supabase
            restClient.delete()
                    .uri("/storage/v1/object/{bucket}/{path}", bucketName, cleanPath)
                    .retrieve()
                    .toBodilessEntity();

        } catch (Exception e) {
            // Solo logueamos el error, no queremos romper el flujo si el archivo ya no existe
            System.err.println("Advertencia: No se pudo borrar el archivo " + pathArchivo + ": " + e.getMessage());
        }
    }

}
