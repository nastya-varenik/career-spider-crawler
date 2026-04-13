package com.nastya;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CareerApplication {
    public static void main(String[] args) {
        // Эта строка запускает веб-сервер Tomcat
        SpringApplication.run(CareerApplication.class, args);
    }
}