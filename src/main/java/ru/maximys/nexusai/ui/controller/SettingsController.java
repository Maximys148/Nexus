package ru.maximys.nexusai.ui.controller;

import atlantafx.base.theme.*;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.maximys.nexusai.backend.config.AppSettings;
import ru.maximys.nexusai.backend.service.SettingService;

@Component
public class SettingsController {

    @Autowired
    private SettingService settingService;
    @Autowired
    private AppSettings appSettings;

    @FXML private ComboBox<String> themeComboBox;

    @FXML
    public void initialize() {
        // Заполняем список названиями
        themeComboBox.getItems().addAll("Primer Light", "Primer Dark", "Nord Light", "Nord Dark", "Cupertino Light", "Cupertino Dark", "Dracula");

        String currentTheme = appSettings.getSavedTheme();
        themeComboBox.setValue(currentTheme);

        // Слушаем изменения и сохраняем их
        themeComboBox.setOnAction(event -> {
            String selected = themeComboBox.getValue();
            settingService.apply(selected);
            appSettings.saveTheme(selected);
        });
    }
}
