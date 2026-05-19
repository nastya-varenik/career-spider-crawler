package com.nastya;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class HhSource implements JobSource {

    @Override
    public List<Vacancy> getVacancies(String query) {
        List<Vacancy> result = new ArrayList<>();
        RestTemplate restTemplate = new RestTemplate();

        try {
            // ФОРМИРУЕМ ЗАГОЛОВКИ, КОТОРЫЕ HH НЕ ЗАБЛОКИРУЕТ
            HttpHeaders headers = new HttpHeaders();
            // Важно: HH просит указывать название приложения в User-Agent
            headers.set("User-Agent", "CareerSpiderApp/1.0 (nastya@example.com)");
            headers.set("Accept", "application/json");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // area=16 (Беларусь), per_page=50 (максимум за раз)
            String url = "https://api.hh.ru/vacancies?text=" + query + "&area=16&per_page=50";

            ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode items = response.getBody().path("items");

                for (JsonNode item : items) {
                    String title = item.path("name").asText();
                    String company = item.path("employer").path("name").asText();
                    String urlVac = item.path("alternate_url").asText();

                    // Обработка зарплаты из JSON
                    String salaryStr = "По договоренности";
                    JsonNode salNode = item.path("salary");
                    if (!salNode.isNull()) {
                        int from = salNode.path("from").asInt(0);
                        int to = salNode.path("to").asInt(0);
                        String cur = salNode.path("currency").asText();

                        if (from > 0 && to > 0) salaryStr = from + " - " + to + " " + cur;
                        else if (from > 0) salaryStr = "от " + from + " " + cur;
                        else if (to > 0) salaryStr = "до " + to + " " + cur;
                    }

                    result.add(new Vacancy(title, company, salaryStr, urlVac, "HH.ru (Rabota.by)"));
                }
            }
        } catch (Exception e) {
            // Если всё же случился 403, мы просто выведем это в консоль, чтобы не вешать программу
            System.err.println("HH.ru API ошибка: " + e.getMessage());
        }
        return result;
    }
}