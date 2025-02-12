package com.citydata.ingestionservice.consumer;

import com.citydata.ingestionservice.constants.TaskStatus;
import com.citydata.ingestionservice.events.TaskStatusEvent;
import com.citydata.ingestionservice.service.TaskStatusUpdateService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskStatusUpdateConsumerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private TaskStatusUpdateService taskStatusUpdateService;

    @InjectMocks
    private TaskStatusUpdateConsumer taskStatusUpdateConsumer;

    private TaskStatusEvent taskStatusEvent;

    @BeforeEach
    void setUp() {
        taskStatusEvent = new TaskStatusEvent();
        taskStatusEvent.setTaskId("123");
        taskStatusEvent.setStatus(TaskStatus.COMPLETED);
        taskStatusEvent.setTimestamp(Instant.now());
    }

    @Test
    void testConsumeTaskStatusUpdate_SuccessfulProcessing() throws JsonProcessingException {
        String jsonMessage = "{ \"taskId\": \"123\", \"status\": \"COMPLETED\", \"timestamp\": \"2024\" }";
        ConsumerRecord<String, String> record = new ConsumerRecord<>("task-status-topic", 0, 0L, "key", jsonMessage);

        when(objectMapper.readValue(jsonMessage, TaskStatusEvent.class)).thenReturn(taskStatusEvent);

        taskStatusUpdateConsumer.consumeTaskStatusUpdate(record);

        verify(objectMapper).readValue(jsonMessage, TaskStatusEvent.class);
        verify(taskStatusUpdateService).updateTaskStatus(taskStatusEvent);
    }

    @Test
    void testConsumeTaskStatusUpdate_InvalidJson_ShouldLogError() throws JsonProcessingException {
        String invalidJsonMessage = "{ invalid_json";
        ConsumerRecord<String, String> record = new ConsumerRecord<>("task-status-topic", 0, 0L, "key", invalidJsonMessage);

        when(objectMapper.readValue(invalidJsonMessage, TaskStatusEvent.class)).thenThrow(new JsonProcessingException("Invalid JSON") {});

        taskStatusUpdateConsumer.consumeTaskStatusUpdate(record);

        verify(objectMapper).readValue(invalidJsonMessage, TaskStatusEvent.class);
        verify(taskStatusUpdateService, never()).updateTaskStatus(any());
    }
}
