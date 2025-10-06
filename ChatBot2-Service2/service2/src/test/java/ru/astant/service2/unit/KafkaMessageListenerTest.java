package ru.astant.service2.unit;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;
import ru.astant.service2.services.KafkaMessageListener;
import ru.astant.service2.services.LlmService;

import java.util.Map;

import static org.mockito.Mockito.*;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class KafkaMessageListenerTest {

    @Mock
    private LlmService llmService;

    @Mock
    private Acknowledgment ack;

    @InjectMocks
    private KafkaMessageListener listener;

    @Test
    void shouldProcessMessageAndSendBack() {
        Map<String, Object> message = Map.of("id", 1, "message", "Hello", "username", "morrasha");
        when(llmService.process("Hello")).thenReturn("Response from LLM");
        listener.listen(message, ack);
        verify(llmService).sendBack(anyString(), eq("Response from LLM"));

    }
}

