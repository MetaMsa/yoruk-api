package com.yoruk.api.services;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Duration;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;


import com.yoruk.api.dto.VisaInfo;
import com.yoruk.api.model.Visa;
import com.yoruk.api.repository.VisaRepository;

import tools.jackson.databind.ObjectMapper;

@Service
public class ScraperService {
    private final VisaRepository visaRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String KEY = "visaInfo:";

    public ScraperService(
            VisaRepository visaRepository,
            RedisTemplate<String, Object> redisTemplate,
            ObjectMapper objectMapper) {

        this.visaRepository = visaRepository;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public VisaInfo getVisaInfo(String common, int passportIndex, String official) {
        String keyName = official;
        String keyPassport = String.valueOf(passportIndex);

        String redisKey = KEY + keyName + ":" + keyPassport;

        Object cached = redisTemplate.opsForValue().get(redisKey);

        if (cached != null) {
            if (cached instanceof VisaInfo dto) {
                return dto;
            }

            return objectMapper.convertValue(cached, VisaInfo.class);
        }

        Duration maxAge = Duration.ofDays(7);

        Visa visa = findFromDb(keyName, passportIndex);

        if (visa != null) {

            boolean stale = visa.getLastUpdated()
                    .isBefore(LocalDateTime.now().minus(maxAge));

            if (!stale) {
                VisaInfo dto = map(visa);
                redisTemplate.opsForValue().set(redisKey, dto, Duration.ofHours(24));
                return dto;
            }

            VisaInfo refreshed;
            try {
                refreshed = scrapeVisaInfo(common, passportIndex, official);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            visa.setVisaInfo(refreshed.visa_info());
            visa.setLastUpdated(LocalDateTime.now());

            visaRepository.save(visa);

            redisTemplate.opsForValue().set(redisKey, refreshed, Duration.ofHours(24));

            return refreshed;
        }

        VisaInfo scraped;
        try {
            scraped = scrapeVisaInfo(common, passportIndex, official);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Visa entity = new Visa();
        entity.setName(keyName);
        entity.setPassport(scraped.passport());
        entity.setVisaInfo(scraped.visa_info());
        entity.setLastUpdated(LocalDateTime.now());

        visaRepository.save(entity);

        redisTemplate.opsForValue().set(redisKey, scraped, Duration.ofHours(24));

        return scraped;
    }

    public VisaInfo scrapeVisaInfo(String common, int passportIndex, String official) throws IOException {
        Document doc = Jsoup.connect(
                "https://tr.wikipedia.org/w/rest.php/v1/page/T%C3%BCrk_vatanda%C5%9Flar%C4%B1n%C4%B1n_tabi_oldu%C4%9Fu_vize_uygulamalar%C4%B1/html?flavor=view&redirect=true&stash=false")
                .userAgent("YorukApi/1.0")
                .get();

        Element match = null;
        String title = null;

        for (Element row : doc.select("tr")) {

            Element link = row.selectFirst("th a[title]");

            if (link == null)
                continue;

            title = link.attr("title").toLowerCase();

            if (title.equals(common.toLowerCase()) || title.equals(official.toLowerCase())) {
                match = row;
                break;
            }
        }

        if (match != null) {

            var tds = match.select("td");

            if (passportIndex >= 0 && passportIndex < tds.size()) {

                Element data = tds.get(passportIndex);

                data.select("a[href*=cite_note]").remove();

                return new VisaInfo(title, passportIndex, data.text());
            }
        }

        return null;
    }

    private Visa findFromDb(String name, int passportIndex) {
        return visaRepository.findByNameAndPassport(name, passportIndex)
                .orElse(null);
    }

    private VisaInfo map(Visa v) {
        return new VisaInfo(
                v.getName(),
                v.getPassport(),
                v.getVisaInfo());
    }
}