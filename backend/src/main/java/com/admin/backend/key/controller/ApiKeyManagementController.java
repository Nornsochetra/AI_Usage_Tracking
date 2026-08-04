package com.admin.backend.key.controller;

import com.admin.backend.key.dto.ApiKeyResponse;
import com.admin.backend.key.dto.CreateApiKeyRequest;
import com.admin.backend.key.dto.CreatedApiKeyResponse;
import com.admin.backend.key.service.ApiKeyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/api-keys")
@RequiredArgsConstructor
public class ApiKeyManagementController {

    private final ApiKeyService apiKeyService;

    @GetMapping
    public List<ApiKeyResponse> getAllApiKeys() {
        return apiKeyService.getAllApiKeys();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreatedApiKeyResponse createApiKey(@RequestBody CreateApiKeyRequest request) {
        return apiKeyService.createApiKey(request);
    }
}
