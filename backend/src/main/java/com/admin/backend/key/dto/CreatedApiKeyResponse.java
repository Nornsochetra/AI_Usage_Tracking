package com.admin.backend.key.dto;

import com.admin.backend.common.enumeration.AiProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Returned only once, at creation time. Contains the full plaintext virtual key
 * so the caller can configure it in Claude Code / Gemini CLI. It is never exposed
 * again by any other endpoint (list endpoints return a masked value only).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatedApiKeyResponse {
    private String id;
    private String userEmail;
    private AiProvider provider;
    private String apiKey;
    private LocalDateTime createdAt;
}
