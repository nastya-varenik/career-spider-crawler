package com.nastya;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class LocalCacheSource implements JobSource {
    @Override
    public List<Vacancy> getVacancies(String query) {
        List<Vacancy> list = new ArrayList<>();
        // Данные из внутреннего реестра (кэш приложения)
        list.add(new Vacancy(query + " (Архивная вакансия)", "ОАО МинскПром", "1600 BYN", "#", "Локальный реестр"));
        list.add(new Vacancy(query + " (Срочно)", "БелПочта", "1200 BYN", "#", "Локальный реестр"));
        return list;
    }
}