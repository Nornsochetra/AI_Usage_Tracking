package com.admin.backend.usage.payload;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AIUsageTelemetryRequest {

    private String userEmail;

    private String model;

    private Long promptTokens;

    private Long completionTokens;
}
