package com.yoruk.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import tools.jackson.databind.JsonNode;

@SpringBootApplication
@RestController
public class ApiApplication {
    private final RestClient restClient;

    public ApiApplication(RestClient restClient) {
        this.restClient = restClient;
    }

    public record CountryDetail(String name, String extract) {
    }

    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }

    @Bean
    public static RestClient restClient() {
        return RestClient.builder()
                .defaultHeader("User-Agent", "YorukApi/1.0 (mserhataslan@hotmail.com)")
                .build();
    }

    @GetMapping("/")
    public String home() {
        return "Yörük projesine hoş geldiniz!";
    }

    @GetMapping("/country/{country}")
    public CountryDetail getCountryDetails(@PathVariable("country") String country) {
        String summary = restClient.get()
                .uri("https://tr.wikipedia.org/api/rest_v1/page/summary/{country}", country)
                .retrieve()
                .body(JsonNode.class).get("extract").asString();

        return new CountryDetail(country, summary);
    }
}