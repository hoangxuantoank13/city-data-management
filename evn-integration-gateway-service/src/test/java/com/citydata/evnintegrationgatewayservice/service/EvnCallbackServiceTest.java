package com.citydata.evnintegrationgatewayservice.service;

import com.citydata.evnintegrationgatewayservice.events.ElectricityUsageEvent;
import com.citydata.evnintegrationgatewayservice.producer.ElectricityUsageKafkaProducer;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EvnCallbackServiceTest {

    @Mock
    private ElectricityUsageKafkaProducer kafkaProducer;

    @InjectMocks
    private EvnCallbackService evnCallbackService;

    private ElectricityUsageEvent validEvent;
    private ElectricityUsageEvent invalidEvent;

    @BeforeEach
    void setUp() {
        validEvent = new ElectricityUsageEvent();
        validEvent.setCustomerId("12345");
        validEvent.setBillingCycle("2025-02");
        validEvent.setConsumption(150.5);

        invalidEvent = new ElectricityUsageEvent(); // Missing required fields
    }

    @Test
    void processCallback_ValidEvent_ShouldSendToKafka() throws JsonProcessingException {
        doNothing().when(kafkaProducer).sendEvent(validEvent);

        evnCallbackService.processCallback(validEvent);

        verify(kafkaProducer).sendEvent(validEvent);
    }

    @Test
    void processCallback_InvalidEvent_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> evnCallbackService.processCallback(invalidEvent));
    }
}
