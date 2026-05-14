package com.yoruk.api.services;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import tools.jackson.databind.JsonNode;

import tools.jackson.databind.ObjectMapper;

import com.yoruk.api.dto.CountryDetail;
import com.yoruk.api.model.Country;
import com.yoruk.api.repository.CountryRepository;

import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class CountryInfoService {

    private final CountryRepository countryRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    private static final String KEY = "countryInfo:";

    public CountryInfoService(
            CountryRepository countryRepository,
            RedisTemplate<String, Object> redisTemplate,
            RestClient restClient,
            ObjectMapper objectMapper) {

        this.countryRepository = countryRepository;
        this.redisTemplate = redisTemplate;
        this.restClient = restClient;
        this.objectMapper = objectMapper;
    }

    public CountryDetail getCountryInfo(String official, String common) {

        String keyName = official;
        String redisKey = KEY + keyName;

        Object cached = redisTemplate.opsForValue().get(redisKey);

        if (cached != null) {
            if (cached instanceof CountryDetail dto) {
                return dto;
            }

            return objectMapper.convertValue(cached, CountryDetail.class);
        }

        Duration maxAge = Duration.ofDays(7);

        Country country = findFromDb(keyName);

        if (country != null) {

            boolean stale = country.getLastUpdated()
                    .isBefore(LocalDateTime.now().minus(maxAge));

            if (!stale) {
                CountryDetail dto = map(country);
                redisTemplate.opsForValue().set(redisKey, dto, Duration.ofHours(24));
                return dto;
            }

            CountryDetail refreshed = fetch(official, common);

            country.setExtract(refreshed.extract());
            country.setLastUpdated(LocalDateTime.now());

            countryRepository.save(country);

            redisTemplate.opsForValue().set(redisKey, refreshed, Duration.ofHours(24));

            return refreshed;
        }

        CountryDetail scraped = fetch(official, common);

        Country entity = new Country();
        entity.setName(keyName);
        entity.setExtract(scraped.extract());
        entity.setLastUpdated(LocalDateTime.now());

        countryRepository.save(entity);

        redisTemplate.opsForValue().set(redisKey, scraped, Duration.ofHours(24));

        return scraped;
    }

    private CountryDetail fetch(String official, String common) {

        try {
            JsonNode node = restClient.get()
                    .uri("https://tr.wikipedia.org/api/rest_v1/page/summary/{country}", official)
                    .retrieve()
                    .body(JsonNode.class);

            String country;

            country = official;

            return new CountryDetail(country, node.get("extract").asString());

        } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
            JsonNode node = restClient.get()
                    .uri("https://tr.wikipedia.org/api/rest_v1/page/summary/{country}", common)
                    .retrieve()
                    .body(JsonNode.class);

            String country = common;

            return new CountryDetail(country, node.get("extract").asString());
        }
    }

    private Country findFromDb(String name) {
        return countryRepository.findByName(name)
                .orElse(null);
    }

    private CountryDetail map(Country c) {
        return new CountryDetail(
                c.getName(),
                c.getExtract());
    }
}