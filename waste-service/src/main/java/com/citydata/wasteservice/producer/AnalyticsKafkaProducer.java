package com.citydata.wasteservice.producer;

import com.citydata.wasteservice.events.AnalyticsEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsKafkaProducer extends KafkaProducer {
    @Value("${kafka.topics.analytics}") // Load from config
    private String topic;

    public AnalyticsKafkaProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        super(kafkaTemplate, objectMapper);
    }

    public void sendEvent(AnalyticsEvent message) throws JsonProcessingException {
        String event = objectMapper.writeValueAsString(message);
        kafkaTemplate.send(topic, event);
    }
}
