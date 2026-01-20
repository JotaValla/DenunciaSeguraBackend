package com.andervalla.msconfigserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class MsConfigServerApplication {

    public static void main(String[] args) {
        System.out.println("GRAFANA_AUTH = " + System.getenv("GRAFANA_AUTH"));
        SpringApplication.run(MsConfigServerApplication.class, args);
    }
}
