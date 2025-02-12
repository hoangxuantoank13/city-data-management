package com.citydata.evnintegrationgatewayservice.producer;

import com.citydata.evnintegrationgatewayservice.events.ElectricityUsageEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ElectricityUsageKafkaProducerTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ElectricityUsageKafkaProducer electricityUsageKafkaProducer;

    @BeforeEach
    void setUp() {
        electricityUsageKafkaProducer = new ElectricityUsageKafkaProducer(kafkaTemplate, objectMapper);
        ReflectionTestUtils.setField(electricityUsageKafkaProducer, "topic", "ingestion-integration-events"); // Set topic manually for test
    }

    @Test
    void sendEvent_Success() throws JsonProcessingException {
        // Given
        ElectricityUsageEvent event = new ElectricityUsageEvent("customer-456", 300.5, "2025-02-12T10:00:00Z");
        String eventJson = "{\"customerId\":\"customer-456\",\"consumption\":300.5,\"billingCycle\":\"2025-02-12T10:00:00Z\"}";

        when(objectMapper.writeValueAsString(event)).thenReturn(eventJson);

        // When
        electricityUsageKafkaProducer.sendEvent(event);

        // Then
        verify(kafkaTemplate, times(1)).send(anyString(), eq(eventJson));
    }

    @Test
    void sendEvent_JsonProcessingException() throws JsonProcessingException {
        // Given
        ElectricityUsageEvent event = new ElectricityUsageEvent("customer-456", 300.5, "2025-02-12T10:00:00Z");
        when(objectMapper.writeValueAsString(event)).thenThrow(new JsonProcessingException("JSON error") {});

        // When & Then
        try {
            electricityUsageKafkaProducer.sendEvent(event);
        } catch (JsonProcessingException e) {
            verify(kafkaTemplate, never()).send(anyString(), anyString());
        }
    }
}
