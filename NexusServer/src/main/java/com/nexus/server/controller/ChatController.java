package com.nexus.server.controller;

import com.nexus.server.dto.AssistantRequestDto;
import com.nexus.server.dto.ChatRequestDto;
import com.nexus.server.dto.ChatResponseDto;
import com.nexus.server.service.AiService;
import com.nexus.server.service.N8nService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final AiService aiService;
    private final N8nService n8nService;

    @PostMapping
    public Mono<ResponseEntity<ChatResponseDto>> sendChatRequest(
            @RequestBody ChatRequestDto requestDto,
            @AuthenticationPrincipal OAuth2AuthenticationToken authentication) {

        log.info("Received chat request for provider: {}", requestDto.getProvider());

        String email = authentication.getPrincipal().getAttribute("email");
        Long userId = getUserIdFromEmail(email);

        return aiService.processChatRequest(userId, requestDto)
            .map(ResponseEntity::ok)
            .onErrorResume(e -> {
                log.error("Error processing chat request", e);
                return Mono.just(ResponseEntity.badRequest().build());
            });
    }

    @PostMapping("/assistant")
    public Mono<ResponseEntity<Map<String, Object>>> sendAssistantRequest(
            @RequestBody AssistantRequestDto requestDto,
            @AuthenticationPrincipal OAuth2AuthenticationToken authentication) {

        log.info("Received assistant request");

        return n8nService.sendToAssistant(requestDto.getMessage(), requestDto.getWorkflowId())
            .map(ResponseEntity::ok)
            .onErrorResume(e -> {
                log.error("Error processing assistant request", e);
                return Mono.just(ResponseEntity.badRequest().body(Map.of("error", e.getMessage())));
            });
    }

    private Long getUserIdFromEmail(String email) {
        // TODO: Implement proper user lookup from database
        // For now, return a placeholder
        return 1L;
    }
}
