package com.admin.backend.playground.dto;

import com.admin.backend.common.enumeration.AiProvider;
import lombok.Data;

@Data
public class PlaygroundRequest {
    private String userId;
    private AiProvider provider;
    private String model;
    private String prompt;
}
