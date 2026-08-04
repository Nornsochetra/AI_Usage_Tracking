package com.admin.backend.key.dto;

import com.admin.backend.common.enumeration.AiProvider;
import lombok.Data;

@Data
public class CreateApiKeyRequest {
    private String userId;
    private AiProvider provider;
}
