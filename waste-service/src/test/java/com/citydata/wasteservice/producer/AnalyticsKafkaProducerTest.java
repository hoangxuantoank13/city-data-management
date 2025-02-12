package com.citydata.wasteservice.producer;

import com.citydata.wasteservice.constants.ServiceType;
import com.citydata.wasteservice.events.AnalyticsEvent;
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
class AnalyticsKafkaProducerTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    private AnalyticsKafkaProducer analyticsKafkaProducer;

    @BeforeEach
    void setUp() {
        analyticsKafkaProducer = new AnalyticsKafkaProducer(kafkaTemplate, objectMapper);
        ReflectionTestUtils.setField(analyticsKafkaProducer, "topic", "analytics-topic"); // Set topic manually for test
    }

    @Test
    void shouldSendAnalyticsEventSuccessfully() throws JsonProcessingException {
        // Given
        var event = new AnalyticsEvent(ServiceType.WATER, 30.5, Instant.now());

        String eventJson = "{\"serviceType\":\"WATER\",\"consumption\":30.5,\"timestamp\":\"2025-02-12T12:00:00Z\"}";

        when(objectMapper.writeValueAsString(event)).thenReturn(eventJson);

        // When
        analyticsKafkaProducer.sendEvent(event);

        // Then
        verify(objectMapper, times(1)).writeValueAsString(event);
        verify(kafkaTemplate, times(1)).send("analytics-topic", eventJson);
    }

    @Test
    void shouldHandleJsonProcessingException() throws JsonProcessingException {
        // Given
        var event = new AnalyticsEvent(ServiceType.WATER, 30.5, Instant.now());

        when(objectMapper.writeValueAsString(event)).thenThrow(new JsonProcessingException("Serialization Error") {});

        // When & Then (Ensure exception is caught and Kafka send is never called)
        try {
            analyticsKafkaProducer.sendEvent(event);
        } catch (JsonProcessingException e) {
            // Expected exception, do nothing
        }

        verify(kafkaTemplate, never()).send(anyString(), anyString());
    }
}
