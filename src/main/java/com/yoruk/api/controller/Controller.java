package com.yoruk.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import com.yoruk.api.services.ScraperService;

@RestController
public class Controller {

    private final RestClient restClient;
    private final ScraperService scraperService;

    public Controller(RestClient restClient, ScraperService scraperService) {
        this.restClient = restClient;
        this.scraperService = scraperService;
    }

    public record CountryDetail(String name, String extract) {
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

    @GetMapping("/visa")
    public String getVisaInfo(
            @RequestParam String country,
            @RequestParam int passportIndex) {

        if (country.equalsIgnoreCase("Türkiye")) {
            return "Serbest Dolaşım Pasaport gerekli değil";
        }

        try {
            String visaInfo = scraperService.scrapeVisaInfo(country, passportIndex);
            if (visaInfo != null) {
                return visaInfo;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Vize durumu bilinmiyor.";
    }
}