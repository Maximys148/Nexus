package ru.maximys.nexusai.backend.service;

import org.springframework.stereotype.Service;

@Service
public class SearchService {
    public String performSearch(String query) {
        if (query == null || query.isEmpty()) {
            return "Запрос не может быть пустым.";
        }
        return "Результат поиска для: " + query + " (обработано в Backend)";
    }
}
