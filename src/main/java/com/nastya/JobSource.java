package com.nastya;

import java.util.List;

public interface JobSource {
    List<Vacancy> getVacancies(String query);
}