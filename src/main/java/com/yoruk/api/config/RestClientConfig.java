package com.yoruk.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public static RestClient restClient() {
        return RestClient.builder()
                .defaultHeader("User-Agent", "YorukApi/1.0 (mserhataslan@hotmail.com)")
                .build();
    }
}
