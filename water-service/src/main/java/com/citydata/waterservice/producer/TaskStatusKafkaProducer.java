package com.citydata.waterservice.producer;

import com.citydata.waterservice.events.TaskStatusEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class TaskStatusKafkaProducer extends KafkaProducer {
    @Value("${kafka.topics.task-status}") // Load from config
    private String topic;

    public TaskStatusKafkaProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        super(kafkaTemplate, objectMapper);
    }

    public void sendEvent(TaskStatusEvent message) throws JsonProcessingException {
        String event = objectMapper.writeValueAsString(message);
        kafkaTemplate.send(topic, event);
    }
}
