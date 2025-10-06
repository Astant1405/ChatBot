package ru.astant.service2.unit;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.client.RestTemplate;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.extension.ExtendWith;
import ru.astant.service2.services.LlmService;

import java.util.Map;

@ExtendWith(MockitoExtension.class)
class LlmServiceTest {

    @InjectMocks
    private LlmService llmService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Test
    void shouldReturnResponseFromOllama() {
        String message = "Hello";
        String expectedResponse = "Response from LLM";

        ResponseEntity<Map> mockResponse = new ResponseEntity<>(
                Map.of("response", expectedResponse),
                HttpStatus.OK
        );
        when(restTemplate.postForEntity(
                eq("http://ollama:11434/api/generate"),
                any(HttpEntity.class),
                eq(Map.class)))
                .thenReturn(mockResponse);

        llmService.process(message);

        verify(kafkaTemplate).send("llm-responses", "anonymous", expectedResponse);
    }
}


