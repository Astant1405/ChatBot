package ru.astant.service1.controllers;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/llm")
public class LlmResponseController {

    private final Map<String, List<String>> responses = new HashMap<>();

    @PostMapping("/response")
    public void saveResponse(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        String response = payload.get("response");
        responses.computeIfAbsent(username, k -> new ArrayList<>()).add(response);
    }
    
    @GetMapping("/responses")
    public List<String> getResponses(@RequestParam String username) {
        return responses.getOrDefault(username, Collections.emptyList());
    }
}
