package ru.astant.service2.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class KafkaMessageListener {

    private final LlmService llmService;

    @Autowired
    public KafkaMessageListener(LlmService llmService) {
        this.llmService = llmService;
    }

    @KafkaListener(topics = "test-topic", containerFactory = "mapKafkaListenerContainerFactory")
    public void listen(Map<String, Object> json, Acknowledgment ack) {
        try {
            String llmResponse = llmService.process(json.get("message").toString());
            llmService.sendBack((String) json.get("username"), llmResponse);
            ack.acknowledge();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
