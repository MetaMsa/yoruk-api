package com.yoruk.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;

import com.yoruk.api.services.GeminiService;
import com.yoruk.api.services.CountryInfoService;
import com.yoruk.api.services.ScraperService;
import com.yoruk.api.services.RateLimitService;

import com.yoruk.api.dto.CountryDetail;
import com.yoruk.api.dto.VisaInfo;
import com.yoruk.api.dto.GeminiRes;

@RestController
public class Controller {
    private final ScraperService scraperService;
    private final GeminiService geminiService;
    private final CountryInfoService countryInfoService;
    private final RateLimitService rateLimitService;

    public Controller(ScraperService scraperService, GeminiService geminiService,
            CountryInfoService countryInfoService, RateLimitService rateLimitService) {
        this.scraperService = scraperService;
        this.geminiService = geminiService;
        this.countryInfoService = countryInfoService;
        this.rateLimitService = rateLimitService;
    }

    @GetMapping("/country")
    public CountryDetail getCountryDetails(
            @RequestParam String official,
            @RequestParam String common) {
        CountryDetail result = countryInfoService.getCountryInfo(official, common);

        if (result == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Ülke bilgisi bulunamadı.");
        }

        return result;
    }

    @GetMapping("/visa")
    public VisaInfo getVisaInfo(
            @RequestParam String common,
            @RequestParam String official,
            @RequestParam int passportIndex) {

        if (common.equalsIgnoreCase("Türkiye") || official.equalsIgnoreCase("Republic of Turkey")) {
            return new VisaInfo("Türkiye", passportIndex, "Serbest Dolaşım Pasaport gerekli değil");
        }

        VisaInfo result = scraperService.getVisaInfo(common, passportIndex, official);

        if (result == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Vize bilgisi bulunamadı.");
        }

        return result;
    }

    @GetMapping("/gemini")
    public GeminiRes getGeminiHint(
            @RequestParam String country,
            @RequestParam String passport,
            HttpServletRequest request) {
        if (country.equalsIgnoreCase("Republic of Turkey"))
            return new GeminiRes("Republic of Turkey", passport, "");

        String xfHeader = request.getHeader("X-Forwarded-For");
        String remoteAddr = request.getRemoteAddr();
        String ip = (xfHeader != null && !xfHeader.isEmpty())
                ? xfHeader.split(",")[0]
                : remoteAddr;

        if (!rateLimitService.isAllowed(ip)) {
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Çok sayıda istek yapıldı.");
        }

        GeminiRes res = geminiService.getGeminiRes(country, passport);

        if (res == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Gemini cevap vermedi.");
        }

        return res;
    }
}