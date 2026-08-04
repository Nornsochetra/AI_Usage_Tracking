package com.admin.backend.key.service;

import com.admin.backend.key.dto.ApiKeyResponse;
import com.admin.backend.key.dto.CreateApiKeyRequest;
import com.admin.backend.key.dto.CreatedApiKeyResponse;

import java.util.List;

public interface ApiKeyService {

    List<ApiKeyResponse> getAllApiKeys();

    CreatedApiKeyResponse createApiKey(CreateApiKeyRequest request);
}
