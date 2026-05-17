package com.maximys.nexus.client.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maximys.nexus.client.backend.model.ChatRequest;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class ChatService {

    @Value("${app.ai.server.url}")
    private String serverUrl;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Async
    public CompletableFuture<String> sendRequestToAi(String prompt, String model, List<String> files, boolean isLocal) {
        try {
            String targetUrl = isLocal ? "http://localhost:11434/api/generate" : serverUrl;
            log.info("[HTTP] Формирование запроса к AI. Целевой URL: {}, Модель: {}", targetUrl, model);

            ChatRequest chatRequest = new ChatRequest(prompt, model, files, isLocal);
            String jsonBody = objectMapper.writeValueAsString(chatRequest);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .timeout(Duration.ofSeconds(60))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        log.info("[HTTP] Получен ответ от сервера. Статус-код: {}", response.statusCode());
                        if (response.statusCode() == 200) {
                            return parseAiResponse(response.body(), isLocal);
                        } else {
                            log.warn("[HTTP] Сервер ответил ошибкой: {}", response.body());
                            return "Ошибка сервера: код " + response.statusCode();
                        }
                    })
                    .exceptionally(ex -> {
                        log.error("[HTTP] Сбой сетевого соединения: ", ex);
                        return "Ошибка сети: " + ex.getMessage();
                    });

        } catch (Exception e) {
            log.error("[HTTP] Ошибка сборки JSON-запроса: ", e);
            return CompletableFuture.completedFuture("Ошибка построения запроса: " + e.getMessage());
        }
    }

    private String parseAiResponse(String responseBody, boolean isLocal) {
        try {
            Map<?, ?> map = objectMapper.readValue(responseBody, Map.class);
            if (isLocal && map.containsKey("response")) return map.get("response").toString();
            if (map.containsKey("output")) return map.get("output").toString();
            if (map.containsKey("text")) return map.get("text").toString();
            return responseBody;
        } catch (Exception e) {
            log.warn("[HTTP] Не удалось распарсить JSON, возврат сырого текста ответа");
            return responseBody;
        }
    }

    public void addMessage(String text, boolean isUser, ScrollPane chatScrollPane, VBox messageContainer) {
        HBox messageBox = new HBox();
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMaxWidth(400);
        label.getStyleClass().add("user-message-base");

        if (isUser) {
            messageBox.setAlignment(Pos.CENTER_RIGHT);
            label.getStyleClass().addAll("accent", "user-message");
            label.setStyle("-fx-background-radius: 15 15 2 15; -fx-padding: 8 12; -fx-font-size: 14px;");
        } else {
            messageBox.setAlignment(Pos.CENTER_LEFT);
            label.getStyleClass().addAll("secondary", "ai-message");
            label.setStyle("-fx-background-radius: 15 15 15 2; -fx-padding: 8 12; -fx-font-size: 14px;");
        }

        messageBox.getChildren().add(label);

        Platform.runLater(() -> {
            messageContainer.getChildren().add(messageBox);
            chatScrollPane.setVvalue(1.0);
            log.debug("[UI] Новое сообщение отрендерено на экране. Автор: {}", isUser ? "USER" : "AI");
        });
    }
}
