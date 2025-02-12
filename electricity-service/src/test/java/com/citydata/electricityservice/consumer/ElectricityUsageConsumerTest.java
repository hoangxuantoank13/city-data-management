package com.citydata.electricityservice.consumer;

import com.citydata.electricityservice.events.AnalyticsEvent;
import com.citydata.electricityservice.events.TaskStatusEvent;
import com.citydata.electricityservice.model.ElectricityUsage;
import com.citydata.electricityservice.producer.AnalyticsKafkaProducer;
import com.citydata.electricityservice.producer.TaskStatusKafkaProducer;
import com.citydata.electricityservice.repository.ElectricityUsageRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ElectricityUsageConsumerTest {

    @InjectMocks
    private ElectricityUsageConsumer electricityUsageConsumer;

    @Mock
    private ElectricityUsageRepository electricityUsageRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private TaskStatusKafkaProducer taskStatusKafkaProducer;

    @Mock
    private AnalyticsKafkaProducer analyticsKafkaProducer;

    private final String kafkaMessage = """
        {
            "id": "task-123",
            "serviceType": "ELECTRICITY",
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
    void shouldProcessElectricityUsageSuccessfully() throws Exception {
        // Given
        ElectricityUsage savedElectricityUsage = ElectricityUsage.builder()
                .id(ObjectId.get().toHexString())
                .taskId("task-123")
                .customerId("customer-456")
                .consumption(20.5)
                .billingCycle("2025-02")
                .createdAt(Instant.now())
                .build();

        when(electricityUsageRepository.save(any(ElectricityUsage.class))).thenReturn(savedElectricityUsage);

        // When
        electricityUsageConsumer.consumeElectricityUsage(kafkaMessage);

        // Then
        verify(electricityUsageRepository, times(1)).save(any(ElectricityUsage.class));
        verify(taskStatusKafkaProducer, times(1)).sendEvent(any(TaskStatusEvent.class));
        verify(analyticsKafkaProducer, times(1)).sendEvent(any(AnalyticsEvent.class));
    }

    @Test
    void shouldHandleInvalidJsonGracefully() throws Exception {
        // Given
        String invalidMessage = "invalid-json";

        when(objectMapper.readTree(invalidMessage)).thenThrow(new RuntimeException("JSON Parse Error"));

        // When
        electricityUsageConsumer.consumeElectricityUsage(invalidMessage);

        // Then
        verify(electricityUsageRepository, never()).save(any(ElectricityUsage.class));
        verify(taskStatusKafkaProducer, never()).sendEvent(any(TaskStatusEvent.class));
        verify(analyticsKafkaProducer, never()).sendEvent(any(AnalyticsEvent.class));
    }
}
