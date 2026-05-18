package com.nastya;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class PracaSource implements JobSource {

    @Override
    public List<Vacancy> getVacancies(String query) {
        List<Vacancy> result = new ArrayList<>();
        try {
            // Формируем URL поиска как на самом сайте
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = "https://praca.by/search/vacancies/?search[query]=" + encodedQuery;

            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/123.0.0.0")
                    .timeout(10000)
                    .get();

            // Находим все карточки вакансий
            Elements elements = doc.select(".vacancies-list__item");

            for (Element el : elements) {
                // Название вакансии и ссылка
                Element titleEl = el.selectFirst(".vacancy-preview-card__title");
                if (titleEl == null) continue;

                String title = titleEl.text();
                String link = titleEl.attr("abs:href");

                // Компания
                String company = el.select(".vacancy-preview-card__company").text();

                // Зарплата
                String salary = el.select(".vacancy-preview-card__salary").text();
                if (salary.isEmpty()) salary = "Договорная";

                result.add(new Vacancy(title, company, salary, link, "Praca.by"));

                // Ограничим количество, чтобы не ждать долго
                if (result.size() >= 15) break;
            }
        } catch (Exception e) {
            System.err.println("Ошибка Praca.by: " + e.getMessage());
        }
        return result;
    }
}