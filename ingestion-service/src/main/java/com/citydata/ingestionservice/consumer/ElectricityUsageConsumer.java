package com.citydata.ingestionservice.consumer;

import com.citydata.ingestionservice.constants.IngestionSource;
import com.citydata.ingestionservice.dto.ElectricityUsageData;
import com.citydata.ingestionservice.service.IngestionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ElectricityUsageConsumer {
    private final IngestionService ingestionService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "#{${kafka.topics.integration-electricity}}", groupId = "#{${kafka.consumer.group-id}}")
    public void consumeElectricityUsageEvent(String message) {
        try {
            ElectricityUsageData event = objectMapper.readValue(message, ElectricityUsageData.class);
            log.info("Received electricity usage event: {}", event);
            ingestionService.processElectricityUsageData(event, IngestionSource.INTEGRATION, null);
        } catch (Exception e) {
            log.error("Error processing electricity usage event: {}", e.getMessage(), e);
        }
    }
}
