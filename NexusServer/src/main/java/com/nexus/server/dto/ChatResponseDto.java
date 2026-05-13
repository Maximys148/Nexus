package com.nexus.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponseDto {
    private String response;
    private String provider;
    private String model;
    private Double tokensUsed;
    private Double cost;
    private Long requestId;
}
