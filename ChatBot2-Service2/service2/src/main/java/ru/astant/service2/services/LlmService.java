package ru.astant.service2.services;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class LlmService {
    private final RestTemplate restTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public LlmService(RestTemplate restTemplate, KafkaTemplate<String, String> kafkaTemplate) {
        this.restTemplate = restTemplate;
        this.kafkaTemplate = kafkaTemplate;
    }

    public String process(String message) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> payload = Map.of("model", "mistral",
                "prompt", message,
                "stream", false);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "http://ollama:11434/api/generate",
                request,
                Map.class
        );

        String answer = (String) response.getBody().get("response");
        kafkaTemplate.send("llm-responses", "anonymous", answer);
        return answer;
    }

    public void sendBack(String username, String answer) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> responsePayload = Map.of(
                "username", username,
                "response", answer
        );

        HttpEntity<Map<String, String>> request = new HttpEntity<>(responsePayload, headers);
        restTemplate.postForEntity("http://service1:8080/api/llm/response", request, Map.class);
    }
}
