package com.admin.backend.playground.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaygroundResponse {
    private boolean success;
    private String model;
    private String responseText;
    private Long promptTokens;
    private Long completionTokens;
    private Double costUsd;
    private String error;
}
