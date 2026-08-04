package com.admin.backend.key.service.impl;

import com.admin.backend.common.domain.ApiKey;
import com.admin.backend.common.domain.User;
import com.admin.backend.common.enumeration.AiProvider;
import com.admin.backend.common.repository.ApiKeyRepository;
import com.admin.backend.common.repository.UserRepository;
import com.admin.backend.key.dto.ApiKeyResponse;
import com.admin.backend.key.dto.CreateApiKeyRequest;
import com.admin.backend.key.dto.CreatedApiKeyResponse;
import com.admin.backend.key.service.ApiKeyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApiKeyServiceImpl implements ApiKeyService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();

    private final ApiKeyRepository apiKeyRepository;
    private final UserRepository userRepository;

    @Override
    public List<ApiKeyResponse> getAllApiKeys() {
        return apiKeyRepository.findAll().stream().map(key -> ApiKeyResponse.builder()
                .id(key.getId())
                .userEmail(key.getUser() != null ? key.getUser().getEmail() : null)
                .userName(key.getUser() != null ? key.getUser().getFullName() : null)
                .provider(key.getProvider())
                .maskedKey(maskKey(key.getKeyValue()))
                .createdAt(key.getCreatedAt())
                .build()
        ).collect(Collectors.toList());
    }

    @Override
    public CreatedApiKeyResponse createApiKey(CreateApiKeyRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Unknown user: " + request.getUserId()));

        if (apiKeyRepository.existsByUserIdAndProvider(user.getId(), request.getProvider())) {
            throw new IllegalArgumentException(
                    "User already has a " + request.getProvider() + " key");
        }

        ApiKey apiKey = new ApiKey();
        apiKey.setUser(user);
        apiKey.setProvider(request.getProvider());
        apiKey.setKeyValue(generateKey(request.getProvider()));

        ApiKey saved = apiKeyRepository.save(apiKey);
        return CreatedApiKeyResponse.builder()
                .id(saved.getId())
                .userEmail(user.getEmail())
                .provider(saved.getProvider())
                .apiKey(saved.getKeyValue())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    private String generateKey(AiProvider provider) {
        byte[] randomBytes = new byte[24];
        SECURE_RANDOM.nextBytes(randomBytes);
        String prefix = provider == AiProvider.ANTHROPIC ? "sk-ant-proxy-" : "sk-gmn-proxy-";
        return prefix + URL_ENCODER.encodeToString(randomBytes);
    }

    private String maskKey(String key) {
        if (key == null || key.length() < 8) return "****";
        return key.substring(0, 4) + "..." + key.substring(key.length() - 4);
    }
}
