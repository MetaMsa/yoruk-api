package com.yoruk.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.yoruk.api.services.ScraperService;
import com.yoruk.api.services.GeminiService;
import com.yoruk.api.services.CountryInfoService;

import com.yoruk.api.dto.CountryDetail;

@RestController
public class Controller {
    private final ScraperService scraperService;
    private final GeminiService geminiService;
    private final CountryInfoService countryInfoService;

    public Controller(ScraperService scraperService, GeminiService geminiService,
            CountryInfoService countryInfoService) {
        this.scraperService = scraperService;
        this.geminiService = geminiService;
        this.countryInfoService = countryInfoService;
    }

    @GetMapping("/")
    public String home() {
        return "Yörük projesine hoş geldiniz!";
    }
    
    @GetMapping("/country")
    public CountryDetail getCountryDetails(
            @RequestParam String official,
            @RequestParam String common) {
            return countryInfoService.getCountryInfo(official, common);
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
        if (country.equalsIgnoreCase("Republic of Turkey"))
            return "";

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