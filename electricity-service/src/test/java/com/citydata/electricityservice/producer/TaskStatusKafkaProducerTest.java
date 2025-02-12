package com.citydata.electricityservice.producer;

import com.citydata.electricityservice.constants.TaskStatus;
import com.citydata.electricityservice.events.TaskStatusEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskStatusKafkaProducerTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    private TaskStatusKafkaProducer taskStatusKafkaProducer;

    @BeforeEach
    void setUp() {
        taskStatusKafkaProducer = new TaskStatusKafkaProducer(kafkaTemplate, objectMapper);
        ReflectionTestUtils.setField(taskStatusKafkaProducer, "topic", "task-status-topic"); // Set topic manually for test
    }

    @Test
    void shouldSendTaskStatusEventSuccessfully() throws JsonProcessingException {
        // Given
        TaskStatusEvent event = new TaskStatusEvent("task-123", TaskStatus.COMPLETED, Instant.now());
        String eventJson = "{\"taskId\":\"task-123\",\"status\":\"COMPLETED\",\"timestamp\":\"2025-02-12T12:00:00Z\"}";

        when(objectMapper.writeValueAsString(event)).thenReturn(eventJson);

        // When
        taskStatusKafkaProducer.sendEvent(event);

        // Then
        verify(objectMapper, times(1)).writeValueAsString(event);
        verify(kafkaTemplate, times(1)).send("task-status-topic", eventJson);
    }

    @Test
    void shouldHandleJsonProcessingException() throws JsonProcessingException {
        // Given
        TaskStatusEvent event = new TaskStatusEvent("task-123", TaskStatus.FAILED, Instant.now());

        when(objectMapper.writeValueAsString(event)).thenThrow(new JsonProcessingException("Serialization Error") {});

        // When & Then (Ensure exception is caught and Kafka send is never called)
        try {
            taskStatusKafkaProducer.sendEvent(event);
        } catch (JsonProcessingException e) {
            // Expected exception, do nothing
        }

        verify(kafkaTemplate, never()).send(anyString(), anyString());
    }
}
