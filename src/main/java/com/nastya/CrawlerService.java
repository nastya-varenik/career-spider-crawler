package com.nastya;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CrawlerService {

    @Autowired
    private List<JobSource> sources;

    private static final List<String> TECH_KEYWORDS = List.of(
            "Spring", "Java", "Python", "SQL", "Docker", "Kubernetes", "AWS", "Git", "React", "Kafka"
    );

    public List<Vacancy> scanWeb(String query) {
        List<Vacancy> allVacancies = new ArrayList<>();

        for (JobSource source : sources) {
            try {
                // Пытаемся получить данные от конкретного сайта
                List<Vacancy> siteVacancies = source.getVacancies(query);

                // Если сайт что-то вернул, добавляем в общий список
                if (siteVacancies != null && !siteVacancies.isEmpty()) {
                    allVacancies.addAll(siteVacancies);
                }
            } catch (Exception e) {
                // Если сайт заблокирован или выдал ошибку, мы просто пишем об этом в консоль
                // Но цикл продолжается! Другие сайты будут обработаны.
                System.err.println("Критическая ошибка источника " + source.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }

        // После того как собрали всё, что смогли, считаем баллы
        for (Vacancy v : allVacancies) {
            v.setScore(calculateScore(v, query));
        }

        // Сортируем: лучшие сверху
        allVacancies.sort(Comparator.comparingInt(Vacancy::getScore).reversed());
        return allVacancies;
    }

    public int calculateScore(Vacancy v, String query) {
        int score = 0;
        String t = v.getTitle().toLowerCase();
        if (t.contains(query.toLowerCase())) score += 100;

        // Бонусы за реальные сайты
        if (v.getSource().equals("Гос. служба занятости")) score += 200;
        if (v.getSource().equals("Praca.by")) score += 150;
        if (v.getSource().equals("Rabota.by")) score += 100;

        // Локальный реестр в самый низ
        if (v.getSource().equals("Локальный реестр")) score -= 500;

        return score;
    }

    public Map<String, Integer> analyzeTechStack(List<Vacancy> vacancies) {
        Map<String, Integer> stats = new HashMap<>();
        for (Vacancy v : vacancies) {
            for (String tech : TECH_KEYWORDS) {
                if (v.getTitle().toLowerCase().contains(tech.toLowerCase())) {
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
        if (salaryText == null || salaryText.contains("Не указана")) return null;
        String clean = salaryText.replaceAll("[^0-9]", " ").trim();
        String[] parts = clean.split("\\s+");
        try {
            if (parts.length >= 2) return (Double.parseDouble(parts[0]) + Double.parseDouble(parts[1])) / 2;
            if (parts.length == 1) return Double.parseDouble(parts[0]);
        } catch (Exception e) { return null; }
        return null;
    }
}