package com.andervalla.msauth.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class CaptchaService {

    @Value("${cloudflare.secret-key}") // Coloca esto en tu application.properties
    private String secretKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String CLOUDFLARE_URL = "https://challenges.cloudflare.com/turnstile/v0/siteverify";

    public boolean validarToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("secret", secretKey);
        requestBody.add("response", token);

        try {
            // Hacemos la petición POST a Cloudflare
            Map<String, Object> response = restTemplate.postForObject(CLOUDFLARE_URL, requestBody, Map.class);

            if (response == null)
                return false;

            return Boolean.TRUE.equals(response.get("success"));
        } catch (Exception e) {
            // En caso de error de conexión con Cloudflare, decidimos si "fail open" o "fail
            // closed".
            // Por seguridad, retornamos false.
            e.printStackTrace();
            return false;
        }
    }
}
