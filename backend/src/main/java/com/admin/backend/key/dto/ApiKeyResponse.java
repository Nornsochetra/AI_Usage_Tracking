package com.admin.backend.key.dto;

import com.admin.backend.common.enumeration.AiProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiKeyResponse {
    private String id;
    private String userEmail;
    private String userName;
    private AiProvider provider;
    private String maskedKey;
    private LocalDateTime createdAt;
}
