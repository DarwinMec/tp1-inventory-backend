package com.upc.tp1inventory.Config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class MLServiceConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        // Timeout de conexión: 5 segundos (5000 ms)
        factory.setConnectTimeout(5000);

        // Timeout de lectura: 60 segundos (60000 ms)
        // Más alto para entrenamientos de ML que pueden tardar
        factory.setReadTimeout(60000);

        return builder
                .requestFactory(() -> factory)
                .build();
    }
}
