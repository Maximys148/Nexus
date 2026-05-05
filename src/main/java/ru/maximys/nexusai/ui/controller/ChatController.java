package ru.maximys.nexusai.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Component;

@Component
public class ChatController {

    @FXML private VBox messageContainer;
    @FXML private TextField userMessageField;
    @FXML private ScrollPane chatScrollPane;

    @FXML
    public void onSendMessage() {
        String userText = userMessageField.getText();
        if (userText.isEmpty()) return;

        // 1. Добавляем сообщение пользователя в контейнер
        Label userLabel = new Label("Вы: " + userText);
        userLabel.getStyleClass().add("title-4"); // Пример стиля из AtlantaFX
        messageContainer.getChildren().add(userLabel);

        // 2. Очищаем поле ввода
        userMessageField.clear();

        // 3. (Имитация) Ответ нейросети через твой сервис
        Label aiLabel = new Label("Nexus AI: Я получил ваш запрос!");
        aiLabel.setWrapText(true); // Чтобы текст переносился
        aiLabel.getStyleClass().add("text-success"); // Зеленый текст
        messageContainer.getChildren().add(aiLabel);

        // 4. Прокрутка вниз
        chatScrollPane.setVvalue(1.0);
    }
}
