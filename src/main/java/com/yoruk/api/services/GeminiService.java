package com.yoruk.api.services;

import org.springframework.stereotype.Service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.ThinkingConfig;

@Service
public class GeminiService {

    private final Client client;

    public GeminiService(Client client) {
        this.client = client;
    }

    public String generateTextFromTextInput(String country, String passport) {
        String prompt = """
                You are a travel assistant.

                Use ONLY the provided data. Do not invent facts.

                User from: Turkey
                Country: %s
                Passport type: %s

                Write:
                - 1 short travel recommendation
                - 1 caution if needed
                - 1 fun fact

                In Turkish.

                Max 100 words.
                """.formatted(country, passport);

        GenerateContentConfig config = GenerateContentConfig
                .builder()
                .thinkingConfig(
                        ThinkingConfig
                                .builder()
                                .thinkingLevel("LOW")
                                .build()
                            )
                .build();

        GenerateContentResponse response = client.models.generateContent(
                "gemini-3.1-flash-lite",
                prompt,
                config
            );

        return response.text();
    }
}