package com.andervalla.msevidencias.controllers.dtos;

import com.fasterxml.jackson.annotation.JsonAlias;

public record SupabaseSignResponse (
        @JsonAlias({"signedUrl", "signedURL", "signed_url", "url"})
        String signedUrl,
        String token
){
}
