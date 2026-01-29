package com.andervalla.msauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Punto de entrada de ms-auth, servidor de autorización y autenticación.
 */
@SpringBootApplication
@EnableFeignClients
public class MsAuthApplication {

    /**
     * Arranca la aplicación Spring Boot.
     */
    public static void main(String[] args) {
        SpringApplication.run(MsAuthApplication.class, args);
    }

}
