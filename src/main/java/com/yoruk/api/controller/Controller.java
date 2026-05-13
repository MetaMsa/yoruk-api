package com.yoruk.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import com.yoruk.api.services.ScraperService;
import com.yoruk.api.services.GeminiService;

import tools.jackson.databind.JsonNode;

@RestController
public class Controller {

    private final RestClient restClient;
    private final ScraperService scraperService;
    private final GeminiService geminiService;

    public Controller(RestClient restClient, ScraperService scraperService, GeminiService geminiService) {
        this.restClient = restClient;
        this.scraperService = scraperService;
        this.geminiService = geminiService;
    }

    public record CountryDetail(String name, String extract) {
    }

    @GetMapping("/")
    public String home() {
        return "Yörük projesine hoş geldiniz!";
    }

    private CountryDetail fetchFromWiki(String country) {

        try {
            JsonNode node = restClient.get()
                    .uri("https://tr.wikipedia.org/api/rest_v1/page/summary/{country}", country)
                    .retrieve()
                    .body(JsonNode.class);

            if (node == null || !node.has("extract")) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }

            return new CountryDetail(
                    country,
                    node.get("extract").asString());

        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Wikipedia verisi bulunamadı: " + country);
        }
    }

    @GetMapping("/country")
    public CountryDetail getCountryDetails(
            @RequestParam String common,
            @RequestParam String official) {

        String primary = (official != null && !official.isBlank()) ? official : common;

        try {
            return fetchFromWiki(primary);

        } catch (ResponseStatusException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND && !primary.equals(common)) {
                return fetchFromWiki(common);
            }
            throw e;
        }
    }

    @GetMapping("/visa")
    public String getVisaInfo(
            @RequestParam String common,
            @RequestParam String official,
            @RequestParam int passportIndex) {

        if (common.equalsIgnoreCase("Türkiye")) {
            return "Serbest Dolaşım Pasaport gerekli değil";
        }

        try {
            String visaInfo = scraperService.scrapeVisaInfo(common, official, passportIndex);
            if (visaInfo != null) {
                return visaInfo;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Vize durumu bilinmiyor.");
    }

    @GetMapping("/gemini")
    public String getGeminiHint(
            @RequestParam String country,
            @RequestParam String passport) {
        try {
            String res = geminiService.generateTextFromTextInput(country, passport);
            if (res != null) {
                return res;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Gemini cevap vermedi.");
    }
}