package com.nastya;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class RabotaBySource implements JobSource {
    @Override
    public List<Vacancy> getVacancies(String query) {
        List<Vacancy> result = new ArrayList<>();
        try {
            // Самый простой URL
            String url = "https://rabota.by/search/vacancy?text=" + query;
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0")
                    .timeout(5000)
                    .get();

            // Ищем по атрибуту data-qa, он самый надежный на этом сайте
            Elements elements = doc.select("[data-qa=vacancy-serp__vacancy]");

            for (Element el : elements) {
                String title = el.select("[data-qa=serp-item__title]").text();
                String link = el.select("[data-qa=serp-item__title]").attr("abs:href");
                String company = el.select("[data-qa=vacancy-serp__vacancy-employer]").text();
                String salary = el.select("[data-qa=vacancy-serp__vacancy-compensation]").text();

                if (!title.isEmpty()) {
                    result.add(new Vacancy(title, company, salary.isEmpty() ? "По договоренности" : salary, link, "Rabota.by"));
                }
            }
        } catch (Exception e) {
            System.err.println("Rabota.by временно недоступен");
        }
        return result;
    }
}