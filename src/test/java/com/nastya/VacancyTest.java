package com.nastya;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VacancyTest {

    CrawlerService crawlerService = new CrawlerService();

    @Test
    void checkJuniorLowScore() {
        // Добавили пятый аргумент "Test Source"
        Vacancy v = new Vacancy("Junior Java Developer", "Google", "Зарплата не указана", "http://...", "Test Source");
        int score = crawlerService.calculateScore(v, "Java");
        // В нашей логике джуниор может иметь и положительный балл, если название совпало,
        // но он точно будет ниже, чем у сеньора.
        assertTrue(score < 100, "Рейтинг джуниора не должен быть слишком высоким");
    }

    @Test
    void checkSeniorHighScore() {
        // Добавили пятый аргумент "Test Source"
        Vacancy v = new Vacancy("Senior Java Developer", "Sber", "1000 USD", "http://...", "Test Source");
        int score = crawlerService.calculateScore(v, "Java");
        // Проверяем, что за Сеньора и наличие Зарплаты баллы начислились (50 за название + 30 за зарплату)
        assertTrue(score > 50, "Сеньор с зарплатой должен получить высокий балл");
    }

    @Test
    void checkNotJavaVacancy() {
        // Добавили пятый аргумент "Test Source"
        Vacancy v = new Vacancy("Python Developer", "Yandex", "Зарплата не указана", "http://...", "Test Source");
        int score = crawlerService.calculateScore(v, "Java");
        // Если в названии нет "Java", то итоговый балл будет низким или 0
        assertTrue(score <= 0, "Вакансия без ключевого слова не должна получать баллы");
    }
}