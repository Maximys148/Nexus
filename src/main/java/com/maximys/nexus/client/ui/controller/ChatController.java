package com.maximys.nexus.client.ui.controller;

import com.maximys.nexus.client.backend.service.ChatService;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import java.util.ArrayList;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;

    @FXML private ScrollPane chatScrollPane;
    @FXML private VBox messageContainer;
    @FXML private TextField userMessageField;

    private final String currentModel = "deepseek-r1:8b";
    private final boolean useLocalModel = false;

    @FXML
    public void onSendMessage() {
        String text = userMessageField.getText().trim();
        if (text.isEmpty()) return;

        log.info("[UI] Пользователь отправляет сообщение: '{}'", text);

        // 1. Мгновенно отрисовываем локально
        chatService.addMessage(text, true, chatScrollPane, messageContainer);
        userMessageField.clear();

        // 2. Делегируем сетевой запрос сервису и асинхронно ждем ответ
        chatService.sendRequestToAi(text, currentModel, new ArrayList<>(), useLocalModel)
                .thenAccept(aiResponse -> {
                    log.info("[UI] Получен ответ от сервиса для рендеринга");
                    chatService.addMessage(aiResponse, false, chatScrollPane, messageContainer);
                });
    }
}
