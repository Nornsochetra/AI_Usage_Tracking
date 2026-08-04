package com.admin.backend.playground.controller;

import com.admin.backend.playground.dto.PlaygroundRequest;
import com.admin.backend.playground.dto.PlaygroundResponse;
import com.admin.backend.playground.service.PlaygroundService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/playground")
@RequiredArgsConstructor
public class PlaygroundController {

    private final PlaygroundService playgroundService;

    @PostMapping("/generate")
    public PlaygroundResponse generate(@RequestBody PlaygroundRequest request) {
        return playgroundService.generate(request);
    }
}
