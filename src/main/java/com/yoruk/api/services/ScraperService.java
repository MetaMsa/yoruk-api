package com.yoruk.api.services;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;

import info.debatty.java.stringsimilarity.Levenshtein;

@Service
public class ScraperService {

    private final Levenshtein levenshtein = new Levenshtein();

    public String scrapeVisaInfo(String country, int passportIndex) throws IOException {

        Document doc = Jsoup.connect(
                "https://tr.wikipedia.org/wiki/T%C3%BCrk_vatanda%C5%9Flar%C4%B1n%C4%B1n_tabi_oldu%C4%9Fu_vize_uygulamalar%C4%B1")
                .userAgent("YorukApi/1.0")
                .get();

        Element bestMatch = null;
        double bestSimilarity = 0;

        String input = country.toLowerCase();

        for (Element row : doc.select("tr")) {

            Element link = row.selectFirst("th a[title]");

            if (link == null)
                continue;

            String title = link.attr("title").toLowerCase();

            if (title.equals(input)) {
                bestMatch = row;
                bestSimilarity = 1.0;
                break;
            }

            double distance = levenshtein.distance(title, input);

            double similarity =
                    1.0 - (distance / Math.max(title.length(), input.length()));

            if (similarity > bestSimilarity) {
                bestSimilarity = similarity;
                bestMatch = row;
            }
        }

        if (bestMatch != null && bestSimilarity > 0.8) {

            var tds = bestMatch.select("td");

            if (passportIndex >= 0 && passportIndex < tds.size()) {

                Element data = tds.get(passportIndex);

                data.select("a[href*=cite_note]").remove();

                return data.text();
            }
        }

        return null;
    }
}