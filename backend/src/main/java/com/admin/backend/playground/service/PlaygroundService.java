package com.admin.backend.playground.service;

import com.admin.backend.playground.dto.PlaygroundRequest;
import com.admin.backend.playground.dto.PlaygroundResponse;

public interface PlaygroundService {

    PlaygroundResponse generate(PlaygroundRequest request);
}
