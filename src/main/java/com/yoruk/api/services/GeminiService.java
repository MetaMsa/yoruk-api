package com.yoruk.api.services;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.ThinkingConfig;
import com.google.genai.Client;

import com.yoruk.api.dto.GeminiRes;
import com.yoruk.api.model.Gemini;
import com.yoruk.api.repository.GeminiRepository;

import tools.jackson.databind.ObjectMapper;

@Service
public class GeminiService {
    private final GeminiRepository geminiRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final Client client;

    private static final String KEY = "geminiRes:";

    public GeminiService(
            GeminiRepository geminiRepository,
            RedisTemplate<String, Object> redisTemplate,
            ObjectMapper objectMapper,
            Client client) {

        this.geminiRepository = geminiRepository;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.client = client;
    }

    public GeminiRes getGeminiRes(String country, String passport) {
        String keyName = country;
        String keyPassport = String.valueOf(passport);

        String redisKey = KEY + keyName + ":" + keyPassport;

        Object cached = redisTemplate.opsForValue().get(redisKey);

        if (cached != null) {
            if (cached instanceof GeminiRes dto) {
                return dto;
            }

            return objectMapper.convertValue(cached, GeminiRes.class);
        }

        Duration maxAge = Duration.ofDays(7);

        Gemini gemini = findFromDb(keyName, passport);

        if (gemini != null) {

            boolean stale = gemini.getLastUpdated()
                    .isBefore(LocalDateTime.now().minus(maxAge));

            if (!stale) {
                GeminiRes dto = map(gemini);
                redisTemplate.opsForValue().set(redisKey, dto, Duration.ofHours(24));
                return dto;
            }

            GeminiRes refreshed;
            try {
                refreshed = askGemini(country, passport);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            gemini.setGeminiRes(refreshed.gemini_res());
            gemini.setLastUpdated(LocalDateTime.now());

            geminiRepository.save(gemini);

            redisTemplate.opsForValue().set(redisKey, refreshed, Duration.ofHours(24));

            return refreshed;
        }

        GeminiRes asked;
        try {
            asked = askGemini(country, passport);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Gemini entity = new Gemini();
        entity.setName(keyName);
        entity.setPassport(asked.passport());
        entity.setGeminiRes(asked.gemini_res());
        entity.setLastUpdated(LocalDateTime.now());

        geminiRepository.save(entity);

        redisTemplate.opsForValue().set(redisKey, asked, Duration.ofHours(24));

        return asked;
    }

    public GeminiRes askGemini(String country, String passport) throws IOException {
        String prompt = """
                You will be given structured input.

                Treat it as factual context.

                Country: %s
                Passport type: %s

                Based on general world knowledge AND these constraints, generate:
                - travel recommendation
                - caution
                - fun fact

                Do NOT say data is missing.

                In Turkish.

                Max 100 words.
                """.formatted(country, passport);

        GenerateContentConfig config = GenerateContentConfig
                .builder()
                .thinkingConfig(
                        ThinkingConfig
                                .builder()
                                .thinkingLevel("LOW")
                                .build())
                .build();

        GenerateContentResponse response = client.models.generateContent(
                "gemini-3.1-flash-lite",
                prompt,
                config);

        return new GeminiRes(country, passport, response.text());
    }

    private Gemini findFromDb(String name, String passport) {
        return geminiRepository.findByNameAndPassport(name, passport)
                .orElse(null);
    }

    private GeminiRes map(Gemini v) {
        return new GeminiRes(
                v.getName(),
                v.getPassport(),
                v.getGeminiRes());
    }
}