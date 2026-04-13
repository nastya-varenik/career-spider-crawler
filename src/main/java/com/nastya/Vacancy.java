package com.nastya;

public class Vacancy {
    // Делаем поля private (хороший тон в Java)
    private String title;
    private String company;
    private String salary;
    private String url;
    private int score;

    public Vacancy(String title, String company, String salary, String url) {
        this.title = title;
        this.company = company;
        this.salary = salary;
        this.url = url;
        this.score = 0;
    }

    // --- ВОТ ЭТИ МЕТОДЫ НУЖНЫ SPRING BOOT ---

    public String getTitle() {
        return title;
    }

    public String getCompany() {
        return company;
    }

    public String getSalary() {
        return salary;
    }

    public String getUrl() {
        return url;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return title + " [" + company + "] - " + salary + " (Score: " + score + ")";
    }
}