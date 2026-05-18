package com.nastya;

public class Vacancy {
    private String title;
    private String company;
    private String salary;
    private String url;
    private String source; // Новый параметр: название сайта
    private int score;

    public Vacancy(String title, String company, String salary, String url, String source) {
        this.title = title;
        this.company = company;
        this.salary = salary;
        this.url = url;
        this.source = source;
        this.score = 0;
    }

    // Геттеры и сеттеры
    public String getTitle() { return title; }
    public String getCompany() { return company; }
    public String getSalary() { return salary; }
    public String getUrl() { return url; }
    public String getSource() { return source; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
}