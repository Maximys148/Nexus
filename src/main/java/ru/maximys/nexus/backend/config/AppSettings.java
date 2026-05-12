package ru.maximys.nexus.backend.config;

import java.util.prefs.Preferences;
import org.springframework.stereotype.Component;

@Component
public class AppSettings {
    private final Preferences prefs = Preferences.userRoot().node("ru.maximys.nexus");
    private static final String THEME_KEY = "selected_theme";

    public void saveTheme(String themeName) {
        prefs.put(THEME_KEY, themeName);
    }

    public String getSavedTheme() {
        // По умолчанию возвращаем Nord Dark, если ничего не сохранено
        return prefs.get(THEME_KEY, "Nord Dark");
    }
}
