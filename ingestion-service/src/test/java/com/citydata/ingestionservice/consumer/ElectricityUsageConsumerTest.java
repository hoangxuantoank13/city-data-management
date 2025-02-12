package com.citydata.ingestionservice.consumer;

import com.citydata.ingestionservice.constants.IngestionSource;
import com.citydata.ingestionservice.dto.ElectricityUsageData;
import com.citydata.ingestionservice.service.IngestionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ElectricityUsageConsumerTest {

    @Mock
    private IngestionService ingestionService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ElectricityUsageConsumer electricityUsageConsumer;

    private ElectricityUsageData electricityUsageData;
    private String validJsonMessage;
    private String invalidJsonMessage;

    @BeforeEach
    void setUp() {
        electricityUsageData = new ElectricityUsageData();
        electricityUsageData.setCustomerId("123");
        electricityUsageData.setConsumption(123.1);
        electricityUsageData.setBillingCycle("2023");

        validJsonMessage = "{ \"customerId\": \"123\", \"consumption\": 123.1, \"billingCycle\": \"2023\", \"timestamp\": \"2024\" }";
        invalidJsonMessage = "{ invalid_json";
    }

    @Test
    void testConsumeElectricityUsageEvent_SuccessfulProcessing() throws Exception {
        when(objectMapper.readValue(validJsonMessage, ElectricityUsageData.class)).thenReturn(electricityUsageData);

        electricityUsageConsumer.consumeElectricityUsageEvent(validJsonMessage);

        verify(objectMapper).readValue(validJsonMessage, ElectricityUsageData.class);
        verify(ingestionService).processElectricityUsageData(electricityUsageData, IngestionSource.INTEGRATION, null);
    }

    @Test
    void testConsumeElectricityUsageEvent_InvalidJson_ShouldLogError() throws Exception {
        when(objectMapper.readValue(invalidJsonMessage, ElectricityUsageData.class)).thenThrow(new JsonProcessingException("Invalid JSON") {});

        electricityUsageConsumer.consumeElectricityUsageEvent(invalidJsonMessage);

        verify(objectMapper).readValue(invalidJsonMessage, ElectricityUsageData.class);
        verify(ingestionService, never()).processElectricityUsageData(any(), any(), any());
    }
}
