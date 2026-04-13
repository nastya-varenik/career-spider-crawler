package com.nastya;

import java.util.List;
import java.util.Random;

public class UserAgentProvider {

    // Список самых популярных браузеров на 2024-2025 год
    private static final List<String> AGENTS = List.of(
            // Chrome на Windows
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            // Chrome на macOS
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            // Firefox на Windows
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/121.0",
            // Firefox на Linux
            "Mozilla/5.0 (X11; Linux x86_64; rv:109.0) Gecko/20100101 Firefox/121.0",
            // Safari на macOS
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.2 Safari/605.1.15",
            // Edge на Windows
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 Edg/120.0.0.0"
    );

    private static final Random RANDOM = new Random();

    // Метод возвращает одну случайную строку из списка
    public static String getRandom() {
        return AGENTS.get(RANDOM.nextInt(AGENTS.size()));
    }
}