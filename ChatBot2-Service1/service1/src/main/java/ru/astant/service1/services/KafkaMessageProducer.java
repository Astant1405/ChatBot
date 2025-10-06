package ru.astant.service1.services;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class KafkaMessageProducer {
    private final KafkaTemplate<String, Map<String, Object>> kafkaTemplate;

    public KafkaMessageProducer(KafkaTemplate<String, Map<String, Object>> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendUser(Map<String, Object> json) {
        kafkaTemplate.send("test-topic", json)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        System.out.println("Message sent successfully: " + json +
                                " to partition " + result.getRecordMetadata().partition() +
                                " offset " + result.getRecordMetadata().offset());
                    } else {
                        System.err.println("Failed to send message: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                });
    }
}
