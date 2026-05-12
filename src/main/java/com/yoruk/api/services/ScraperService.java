package com.yoruk.api.services;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

@Service
public class ScraperService {

    public String scrapeVisaInfo(String common, String official, int passportIndex) throws IOException {

        Document doc = Jsoup.connect(
                "https://tr.wikipedia.org/w/rest.php/v1/page/T%C3%BCrk_vatanda%C5%9Flar%C4%B1n%C4%B1n_tabi_oldu%C4%9Fu_vize_uygulamalar%C4%B1/html?flavor=view&redirect=true&stash=false")
                .userAgent("YorukApi/1.0")
                .get();

        Element match = null;

        for (Element row : doc.select("tr")) {

            Element link = row.selectFirst("th a[title]");

            if (link == null)
                continue;

            String title = link.attr("title").toLowerCase();

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

                return data.text();
            }
        }

        return null;
    }
}