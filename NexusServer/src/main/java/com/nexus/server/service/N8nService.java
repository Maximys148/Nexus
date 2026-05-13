package com.nexus.server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class N8nService {

    private final WebClient.Builder webClientBuilder;

    @Value("${nexus.ai.n8n.webhook-url}")
    private String n8nWebhookUrl;

    @Value("${nexus.ai.n8n.timeout}")
    private Long timeout;

    public Mono<Map<String, Object>> sendToAssistant(String message, String workflowId) {
        log.info("Sending request to n8n assistant");

        Map<String, Object> requestBody = Map.of(
            "message", message,
            "workflowId", workflowId,
            "timestamp", System.currentTimeMillis()
        );

        return webClientBuilder.build()
            .post()
            .uri(n8nWebhookUrl)
            .header("Content-Type", "application/json")
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(Map.class)
            .timeout(java.time.Duration.ofMillis(timeout))
            .onErrorResume(e -> {
                log.error("Error calling n8n assistant", e);
                return Mono.error(e);
            });
    }
}
