package com.nexus.server.service;

import com.nexus.server.dto.ChatRequestDto;
import com.nexus.server.dto.ChatResponseDto;
import com.nexus.server.model.ChatRequest;
import com.nexus.server.model.RequestStatus;
import com.nexus.server.model.User;
import com.nexus.server.repository.ChatRequestRepository;
import com.nexus.server.repository.UserRepository;
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
public class AiService {

    private final ChatRequestRepository chatRequestRepository;
    private final UserRepository userRepository;
    private final WebClient.Builder webClientBuilder;

    @Value("${nexus.ai.providers.openai.api-key}")
    private String openAiApiKey;

    @Value("${nexus.ai.providers.openai.base-url}")
    private String openAiBaseUrl;

    @Value("${nexus.ai.providers.anthropic.api-key}")
    private String anthropicApiKey;

    @Value("${nexus.ai.providers.anthropic.base-url}")
    private String anthropicBaseUrl;

    @Value("${nexus.ai.providers.gemini.api-key}")
    private String geminiApiKey;

    @Value("${nexus.ai.providers.gemini.base-url}")
    private String geminiBaseUrl;

    public Mono<ChatResponseDto> processChatRequest(Long userId, ChatRequestDto requestDto) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getBalance() <= 0) {
            throw new RuntimeException("Insufficient balance");
        }

        ChatRequest chatRequest = ChatRequest.builder()
            .user(user)
            .provider(requestDto.getProvider())
            .model(requestDto.getModel())
            .message(requestDto.getMessage())
            .tokensUsed(0.0)
            .cost(0.0)
            .status(RequestStatus.PENDING)
            .build();

        chatRequest = chatRequestRepository.save(chatRequest);

        return switch (requestDto.getProvider().toLowerCase()) {
            case "openai" -> sendToOpenAi(requestDto, chatRequest.getId());
            case "anthropic" -> sendToAnthropic(requestDto, chatRequest.getId());
            case "gemini" -> sendToGemini(requestDto, chatRequest.getId());
            default -> Mono.error(new IllegalArgumentException("Unknown provider: " + requestDto.getProvider()));
        };
    }

    private Mono<ChatResponseDto> sendToOpenAi(ChatRequestDto requestDto, Long requestId) {
        log.info("Sending request to OpenAI");

        Map<String, Object> requestBody = Map.of(
            "model", requestDto.getModel(),
            "messages", new Object[]{Map.of("role", "user", "content", requestDto.getMessage())}
        );

        return webClientBuilder.build()
            .post()
            .uri(openAiBaseUrl + "/chat/completions")
            .header("Authorization", "Bearer " + openAiApiKey)
            .header("Content-Type", "application/json")
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(Map.class)
            .map(response -> {
                Map<String, Object> choices = ((java.util.List<Map<String, Object>>) response.get("choices")).get(0);
                Map<String, Object> message = (Map<String, Object>) choices.get("message");
                Map<String, Object> usage = (Map<String, Object>) response.get("usage");

                String content = (String) message.get("content");
                Double tokensUsed = ((Number) usage.get("total_tokens")).doubleValue();
                Double cost = calculateCost(tokensUsed, "openai");

                updateChatRequest(requestId, content, tokensUsed, cost, RequestStatus.COMPLETED);

                return ChatResponseDto.builder()
                    .response(content)
                    .provider("openai")
                    .model(requestDto.getModel())
                    .tokensUsed(tokensUsed)
                    .cost(cost)
                    .requestId(requestId)
                    .build();
            })
            .onErrorResume(e -> {
                log.error("Error calling OpenAI", e);
                updateChatRequest(requestId, null, 0.0, 0.0, RequestStatus.FAILED);
                return Mono.error(e);
            });
    }

    private Mono<ChatResponseDto> sendToAnthropic(ChatRequestDto requestDto, Long requestId) {
        log.info("Sending request to Anthropic");

        Map<String, Object> requestBody = Map.of(
            "model", requestDto.getModel(),
            "max_tokens", 1024,
            "messages", new Object[]{Map.of("role", "user", "content", requestDto.getMessage())}
        );

        return webClientBuilder.build()
            .post()
            .uri(anthropicBaseUrl + "/messages")
            .header("x-api-key", anthropicApiKey)
            .header("Content-Type", "application/json")
            .header("anthropic-version", "2023-06-01")
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(Map.class)
            .map(response -> {
                Map<String, Object> content = ((java.util.List<Map<String, Object>>) response.get("content")).get(0);
                String text = (String) content.get("text");
                Map<String, Object> usage = (Map<String, Object>) response.get("usage");

                Double tokensUsed = ((Number) usage.get("total_tokens")).doubleValue();
                Double cost = calculateCost(tokensUsed, "anthropic");

                updateChatRequest(requestId, text, tokensUsed, cost, RequestStatus.COMPLETED);

                return ChatResponseDto.builder()
                    .response(text)
                    .provider("anthropic")
                    .model(requestDto.getModel())
                    .tokensUsed(tokensUsed)
                    .cost(cost)
                    .requestId(requestId)
                    .build();
            })
            .onErrorResume(e -> {
                log.error("Error calling Anthropic", e);
                updateChatRequest(requestId, null, 0.0, 0.0, RequestStatus.FAILED);
                return Mono.error(e);
            });
    }

    private Mono<ChatResponseDto> sendToGemini(ChatRequestDto requestDto, Long requestId) {
        log.info("Sending request to Gemini");

        Map<String, Object> requestBody = Map.of(
            "contents", new Object[]{Map.of("parts", new Object[]{Map.of("text", requestDto.getMessage())})}
        );

        return webClientBuilder.build()
            .post()
            .uri(geminiBaseUrl + "/models/" + requestDto.getModel() + ":generateContent?key=" + geminiApiKey)
            .header("Content-Type", "application/json")
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(Map.class)
            .map(response -> {
                Map<String, Object> candidates = ((java.util.List<Map<String, Object>>) response.get("candidates")).get(0);
                Map<String, Object> content = (Map<String, Object>) candidates.get("content");
                java.util.List<Map<String, Object>> parts = (java.util.List<Map<String, Object>>) content.get("parts");
                String text = (String) parts.get(0).get("text");

                Double tokensUsed = 0.0;
                if (response.containsKey("usageMetadata")) {
                    Map<String, Object> usage = (Map<String, Object>) response.get("usageMetadata");
                    tokensUsed = ((Number) usage.get("totalTokenCount")).doubleValue();
                }

                Double cost = calculateCost(tokensUsed, "gemini");

                updateChatRequest(requestId, text, tokensUsed, cost, RequestStatus.COMPLETED);

                return ChatResponseDto.builder()
                    .response(text)
                    .provider("gemini")
                    .model(requestDto.getModel())
                    .tokensUsed(tokensUsed)
                    .cost(cost)
                    .requestId(requestId)
                    .build();
            })
            .onErrorResume(e -> {
                log.error("Error calling Gemini", e);
                updateChatRequest(requestId, null, 0.0, 0.0, RequestStatus.FAILED);
                return Mono.error(e);
            });
    }

    private void updateChatRequest(Long requestId, String response, Double tokensUsed, Double cost, RequestStatus status) {
        chatRequestRepository.findById(requestId).ifPresent(request -> {
            request.setResponse(response);
            request.setTokensUsed(tokensUsed);
            request.setCost(cost);
            request.setStatus(status);

            if (status == RequestStatus.COMPLETED && cost > 0) {
                User user = request.getUser();
                user.setBalance(user.getBalance() - cost);
                userRepository.save(user);
            }

            chatRequestRepository.save(request);
        });
    }

    private Double calculateCost(Double tokens, String provider) {
        return switch (provider.toLowerCase()) {
            case "openai" -> tokens * 0.002;
            case "anthropic" -> tokens * 0.003;
            case "gemini" -> tokens * 0.001;
            default -> tokens * 0.002;
        };
    }
}
