package com.citydata.ingestionservice.consumer;

import com.citydata.ingestionservice.events.TaskStatusEvent;
import com.citydata.ingestionservice.service.TaskStatusUpdateService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskStatusUpdateConsumer {
    private final ObjectMapper objectMapper;
    private final TaskStatusUpdateService taskStatusUpdateService;

    @KafkaListener(topics = "${kafka.topics.task-status}", groupId = "#{${kafka.consumer.group-id}}")
    public void consumeTaskStatusUpdate(ConsumerRecord<String, String> record) {
        try {
            TaskStatusEvent event = objectMapper.readValue(record.value(), TaskStatusEvent.class);
            log.info("Received task status update: {}", event);
            taskStatusUpdateService.updateTaskStatus(event);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize task status update event", e);
        }
    }
}
