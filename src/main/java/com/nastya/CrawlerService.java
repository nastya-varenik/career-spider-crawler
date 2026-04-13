package com.nastya;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CrawlerService {

    private static final Logger logger = LoggerFactory.getLogger(CrawlerService.class);

    // Словарь технологий (семантические узлы)
    private static final List<String> TECH_KEYWORDS = List.of(
            "Spring", "Java", "Python", "SQL", "Docker", "Kubernetes", "AWS", "Git", "Linux",
            "React", "Angular", "Vue", "Node", "Kafka", "Redis", "Hibernate", "PostgreSQL",
            "MySQL", "Oracle", "Rest", "Soap", "Maven", "Gradle", "CI/CD", "Kotlin", "Go", "PHP"
    );

    // Курсы валют для приведения к BYN
    private static final double USD_RATE = 3.3;
    private static final double EUR_RATE = 3.5;
    private static final double RUB_RATE = 0.034;

    public List<Vacancy> scanWeb(String query) {
        List<Vacancy> allVacancies = new ArrayList<>();
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper mapper = new ObjectMapper();

        // Формируем запрос к API: ищем по тексту, регион 16 (Беларусь)
        String baseUrl = "https://api.hh.ru/vacancies?text=" + query.replace(" ", "+") + "&area=16&per_page=20";

        try {
            // Парсим первые 3 страницы (60 вакансий)
            for (int page = 0; page < 3; page++) {
                String url = baseUrl + "&page=" + page;
                String response = restTemplate.getForObject(url, String.class);
                JsonNode root = mapper.readTree(response);
                JsonNode items = root.path("items");

                for (JsonNode item : items) {
                    String title = item.path("name").asText();
                    String company = item.path("employer").path("name").asText();
                    String link = item.path("alternate_url").asText();

                    // Извлечение зарплаты (Семантический анализ атрибутов)
                    String salaryStr = "Зарплата не указана";
                    JsonNode salNode = item.path("salary");
                    if (!salNode.isMissingNode() && !salNode.isNull()) {
                        int from = salNode.path("from").asInt(0);
                        int to = salNode.path("to").asInt(0);
                        String currency = salNode.path("currency").asText("");

                        if (from > 0 && to > 0) salaryStr = from + " - " + to + " " + currency;
                        else if (from > 0) salaryStr = "от " + from + " " + currency;
                        else if (to > 0) salaryStr = "до " + to + " " + currency;
                    }

                    Vacancy v = new Vacancy(title, company, salaryStr, link);
                    v.setScore(calculateScore(v, query));
                    allVacancies.add(v);
                }
            }
        } catch (Exception e) {
            logger.error("Ошибка при сканировании API: ", e);
        }

        allVacancies.sort(Comparator.comparingInt(Vacancy::getScore).reversed());
        return allVacancies;
    }

    public Map<String, Integer> analyzeTechStack(List<Vacancy> vacancies) {
        Map<String, Integer> stats = new HashMap<>();
        for (Vacancy v : vacancies) {
            String textToCheck = v.getTitle().toLowerCase();
            for (String tech : TECH_KEYWORDS) {
                if (textToCheck.contains(tech.toLowerCase())) {
                    stats.put(tech, stats.getOrDefault(tech, 0) + 1);
                }
            }
        }
        return stats.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(10)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    public Double parseSalaryToNumber(String salaryText) {
        if (salaryText == null || salaryText.contains("не указана")) return null;

        double multiplier = 1.0;
        String lower = salaryText.toLowerCase();
        if (lower.contains("usd") || lower.contains("$")) multiplier = USD_RATE;
        else if (lower.contains("eur") || lower.contains("€")) multiplier = EUR_RATE;
        else if (lower.contains("rub") || lower.contains("rur")) multiplier = RUB_RATE;

        String cleanText = salaryText.replaceAll("[^0-9]", " ").trim();
        String[] parts = cleanText.split("\\s+");
        List<Double> numbers = new ArrayList<>();

        for (String p : parts) {
            if (!p.isEmpty()) {
                try { numbers.add(Double.parseDouble(p)); } catch (Exception ignored) {}
            }
        }

        if (numbers.isEmpty()) return null;
        double rawSalary = (numbers.size() >= 2) ? (numbers.get(0) + numbers.get(1)) / 2.0 : numbers.get(0);
        return rawSalary * multiplier;
    }

    // Сделали метод public, чтобы он работал в твоих тестах!
    public int calculateScore(Vacancy vacancy, String query) {
        int score = 0;
        String title = vacancy.getTitle().toLowerCase();
        if (title.contains(query.toLowerCase())) score += 10;
        if (title.contains("junior")) score -= 20;
        if (title.contains("senior")) score += 15;
        if (!vacancy.getSalary().contains("не указана")) score += 25;
        return score;
    }
}