package com.nastya;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VacancyTest {

    CrawlerService crawlerService = new CrawlerService();

    @Test
    void checkJuniorLowScore() {
        Vacancy v = new Vacancy("Junior Java Developer", "Google", "Зарплата не указана", "http://...");
        int score = crawlerService.calculateScore(v, "Java");
        assertTrue(score < 0, "Джуниоры должны получать низкий рейтинг!");
    }

    @Test
    void checkSeniorHighScore() {
        Vacancy v = new Vacancy("Senior Java Developer", "Sber", "1000 USD", "http://...");
        int score = crawlerService.calculateScore(v, "Java");
        assertEquals(50, score, "Сеньор с зарплатой должен получить 50 баллов");
    }

    @Test
    void checkNotJavaVacancy() {
        Vacancy v = new Vacancy("Python Developer", "Yandex", "Зарплата не указана", "http://...");
        int score = crawlerService.calculateScore(v, "Java");
        assertEquals(0, score);
    }
}