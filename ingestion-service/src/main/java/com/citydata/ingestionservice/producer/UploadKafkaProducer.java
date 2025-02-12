package com.citydata.ingestionservice.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class UploadKafkaProducer extends KafkaProducer {
    @Value("${kafka.topics.uploads}") // Load from config
    private String topic;

    public UploadKafkaProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        super(kafkaTemplate, objectMapper);
    }

    public void sendEvent(String message) {
        kafkaTemplate.send(topic, message);
    }
}
