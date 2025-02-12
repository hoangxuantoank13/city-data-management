package com.citydata.wasteservice.consumer;

import com.citydata.wasteservice.events.AnalyticsEvent;
import com.citydata.wasteservice.events.TaskStatusEvent;
import com.citydata.wasteservice.model.WasteUsage;
import com.citydata.wasteservice.producer.AnalyticsKafkaProducer;
import com.citydata.wasteservice.producer.TaskStatusKafkaProducer;
import com.citydata.wasteservice.repository.WasteUsageRepository;
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
class WasteUsageConsumerTest {

    @InjectMocks
    private WasteUsageConsumer wasteUsageConsumer;

    @Mock
    private WasteUsageRepository wasteUsageRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private TaskStatusKafkaProducer taskStatusKafkaProducer;

    @Mock
    private AnalyticsKafkaProducer analyticsKafkaProducer;

    private final String kafkaMessage = """
        {
            "id": "task-123",
            "serviceType": "WASTE",
            "rawData": {
                "customerId": "customer-456",
                "wasteAmount": 20.5,
                "collectionDate": "2025-02"
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
    void shouldProcessWasteUsageSuccessfully() throws Exception {
        // Given
        WasteUsage savedWasteUsage = WasteUsage.builder()
                .id(ObjectId.get().toHexString())
                .taskId("task-123")
                .customerId("customer-456")
                .wasteAmount(20.5)
                .collectionDate("2025-02")
                .createdAt(Instant.now())
                .build();

        when(wasteUsageRepository.save(any(WasteUsage.class))).thenReturn(savedWasteUsage);

        // When
        wasteUsageConsumer.consumeWasteUsage(kafkaMessage);

        // Then
        verify(wasteUsageRepository, times(1)).save(any(WasteUsage.class));
        verify(taskStatusKafkaProducer, times(1)).sendEvent(any(TaskStatusEvent.class));
        verify(analyticsKafkaProducer, times(1)).sendEvent(any(AnalyticsEvent.class));
    }

    @Test
    void shouldHandleInvalidJsonGracefully() throws Exception {
        // Given
        String invalidMessage = "invalid-json";

        when(objectMapper.readTree(invalidMessage)).thenThrow(new RuntimeException("JSON Parse Error"));

        // When
        wasteUsageConsumer.consumeWasteUsage(invalidMessage);

        // Then
        verify(wasteUsageRepository, never()).save(any(WasteUsage.class));
        verify(taskStatusKafkaProducer, never()).sendEvent(any(TaskStatusEvent.class));
        verify(analyticsKafkaProducer, never()).sendEvent(any(AnalyticsEvent.class));
    }
}
