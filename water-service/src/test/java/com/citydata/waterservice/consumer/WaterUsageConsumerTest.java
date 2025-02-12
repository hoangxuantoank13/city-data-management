package com.citydata.waterservice.consumer;

import com.citydata.waterservice.events.AnalyticsEvent;
import com.citydata.waterservice.events.TaskStatusEvent;
import com.citydata.waterservice.model.WaterUsage;
import com.citydata.waterservice.producer.AnalyticsKafkaProducer;
import com.citydata.waterservice.producer.TaskStatusKafkaProducer;
import com.citydata.waterservice.repository.WaterUsageRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WaterUsageConsumerTest {

    @InjectMocks
    private WaterUsageConsumer waterUsageConsumer;

    @Mock
    private WaterUsageRepository waterUsageRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private TaskStatusKafkaProducer taskStatusKafkaProducer;

    @Mock
    private AnalyticsKafkaProducer analyticsKafkaProducer;

    private final String kafkaMessage = """
        {
            "id": "task-123",
            "serviceType": "WATER",
            "rawData": {
                "customerId": "customer-456",
                "consumption": 20.5,
                "billingCycle": "2025-02"
            }
        }
        """;

    private JsonNode jsonNode;

    @BeforeEach
    void setUp() throws Exception {
        jsonNode = new ObjectMapper().readTree(kafkaMessage);
        lenient().when(objectMapper.readTree(kafkaMessage)).thenReturn(jsonNode);
    }

    @Test
    void shouldProcessWaterUsageSuccessfully() throws Exception {
        // Given
        WaterUsage savedWaterUsage = WaterUsage.builder()
                .id(ObjectId.get().toHexString())
                .taskId("task-123")
                .customerId("customer-456")
                .consumption(20.5)
                .billingCycle("2025-02")
                .createdAt(Instant.now())
                .build();

        when(waterUsageRepository.save(any(WaterUsage.class))).thenReturn(savedWaterUsage);

        // When
        waterUsageConsumer.consumeWaterUsage(kafkaMessage);

        // Then
        verify(waterUsageRepository, times(1)).save(any(WaterUsage.class));
        verify(taskStatusKafkaProducer, times(1)).sendEvent(any(TaskStatusEvent.class));
        verify(analyticsKafkaProducer, times(1)).sendEvent(any(AnalyticsEvent.class));
    }

    @Test
    void shouldHandleInvalidJsonGracefully() throws Exception {
        // Given
        String invalidMessage = "invalid-json";

        when(objectMapper.readTree(invalidMessage)).thenThrow(new RuntimeException("JSON Parse Error"));

        // When
        waterUsageConsumer.consumeWaterUsage(invalidMessage);

        // Then
        verify(waterUsageRepository, never()).save(any(WaterUsage.class));
        verify(taskStatusKafkaProducer, never()).sendEvent(any(TaskStatusEvent.class));
        verify(analyticsKafkaProducer, never()).sendEvent(any(AnalyticsEvent.class));
    }
}
