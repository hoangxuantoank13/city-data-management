package com.citydata.evnintegrationgatewayservice.producer;

import com.citydata.evnintegrationgatewayservice.events.ElectricityUsageEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ElectricityUsageKafkaProducer extends KafkaProducer {
    @Value("${kafka.topics.integration}") // Load from config
    private String topic;

    public ElectricityUsageKafkaProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        super(kafkaTemplate, objectMapper);
    }

    public void sendEvent(ElectricityUsageEvent message) throws JsonProcessingException {
        String event = objectMapper.writeValueAsString(message);
        kafkaTemplate.send(topic, event);
    }
}
