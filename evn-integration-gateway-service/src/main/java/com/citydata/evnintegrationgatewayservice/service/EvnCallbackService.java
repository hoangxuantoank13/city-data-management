package com.citydata.evnintegrationgatewayservice.service;


import com.citydata.evnintegrationgatewayservice.events.ElectricityUsageEvent;
import com.citydata.evnintegrationgatewayservice.producer.ElectricityUsageKafkaProducer;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EvnCallbackService {
    private final ElectricityUsageKafkaProducer kafkaProducer;

    public void processCallback(ElectricityUsageEvent event) throws JsonProcessingException {
        validateEvent(event);
        kafkaProducer.sendEvent(event);
    }

    private void validateEvent(ElectricityUsageEvent event) {
        if (event.getCustomerId() == null || event.getBillingCycle() == null) {
            throw new IllegalArgumentException("Invalid EVN callback data: Missing required fields");
        }
    }
}

