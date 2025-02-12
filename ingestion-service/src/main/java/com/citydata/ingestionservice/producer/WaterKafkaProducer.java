package com.citydata.ingestionservice.producer;

import com.citydata.ingestionservice.model.Task;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class WaterKafkaProducer extends KafkaProducer {
    @Value("${kafka.topics.water}") // Load from config
    private String topic;

    public WaterKafkaProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        super(kafkaTemplate, objectMapper);
    }

    public void sendEvent(Task message) throws JsonProcessingException {
        String event = objectMapper.writeValueAsString(message);
        kafkaTemplate.send(topic, event);
    }
}
