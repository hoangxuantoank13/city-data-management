package com.citydata.ingestionservice.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducer {
    protected final KafkaTemplate<String, String> kafkaTemplate;
    protected final ObjectMapper objectMapper;

    public void sendEvent(String topic, String message) {
        kafkaTemplate.send(topic, message);
    }
}
