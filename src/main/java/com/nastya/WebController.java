package com.nastya;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Controller
public class WebController {

    @Autowired
    private CrawlerService crawlerService;

    // Кэш для хранения последних результатов (чтобы скачать Excel без повторного парсинга)
    private List<Vacancy> lastVacancies = new ArrayList<>();

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/search")
    public String search(@RequestParam String query, Model model) {
        List<Vacancy> vacancies = crawlerService.scanWeb(query);
        lastVacancies = vacancies; // Сохраняем для экспорта

        model.addAttribute("vacancies", vacancies);
        model.addAttribute("query", query);
        model.addAttribute("count", vacancies.size());

        // 1. Финансовая аналитика
        List<Double> salaries = new ArrayList<>();
        for (Vacancy v : vacancies) {
            Double val = crawlerService.parseSalaryToNumber(v.getSalary());
            if (val != null) salaries.add(val);
        }

        if (!salaries.isEmpty()) {
            double sum = 0;
            double min = Double.MAX_VALUE;
            double max = 0;
            for (Double s : salaries) {
                sum += s;
                if (s < min) min = s;
                if (s > max) max = s;
            }
            model.addAttribute("avgSalary", Math.round(sum / salaries.size()));
            model.addAttribute("minSalary", Math.round(min));
            model.addAttribute("maxSalary", Math.round(max));
        } else {
            model.addAttribute("avgSalary", 0);
        }

        // 2. График Топ Компаний (Пончик)
        Map<String, Integer> companyStats = new HashMap<>();
        for (Vacancy v : vacancies) {
            String companyName = v.getCompany();
            companyStats.put(companyName, companyStats.getOrDefault(companyName, 0) + 1);
        }
        model.addAttribute("chartLabels", getTopKeys(companyStats));
        model.addAttribute("chartData", getTopValues(companyStats));

        // 3. НОВИНКА: График Технологий (Столбики)
        Map<String, Integer> techStats = crawlerService.analyzeTechStack(vacancies);
        model.addAttribute("techLabels", techStats.keySet());
        model.addAttribute("techData", techStats.values());

        return "index";
    }

    // --- СКАЧИВАНИЕ EXCEL (CSV) ---
    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> downloadCsv() {
        // Генерируем CSV в памяти
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(out, false, StandardCharsets.UTF_8)) {
            writer.write('\ufeff'); // BOM для Excel
            writer.println("Название;Компания;Зарплата;Рейтинг;Ссылка");
            for (Vacancy v : lastVacancies) {
                writer.println(String.format("%s;%s;%s;%d;%s",
                        v.getTitle(), v.getCompany(), v.getSalary(), v.getScore(), v.getUrl()));
            }
        }

        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(out.toByteArray()));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=vacancies.csv")
                .contentType(MediaType.parseMediaType("application/csv"))
                .body(resource);
    }

    // Вспомогательные методы для сортировки карт
    private List<String> getTopKeys(Map<String, Integer> map) {
        return map.entrySet().stream().sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(5).map(Map.Entry::getKey).toList();
    }
    private List<Integer> getTopValues(Map<String, Integer> map) {
        return map.entrySet().stream().sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(5).map(Map.Entry::getValue).toList();
    }
}