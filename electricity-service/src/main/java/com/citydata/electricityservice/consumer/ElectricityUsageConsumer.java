package com.citydata.electricityservice.consumer;

import com.citydata.electricityservice.constants.ServiceType;
import com.citydata.electricityservice.constants.TaskStatus;
import com.citydata.electricityservice.events.AnalyticsEvent;
import com.citydata.electricityservice.events.TaskStatusEvent;
import com.citydata.electricityservice.model.ElectricityUsage;
import com.citydata.electricityservice.producer.AnalyticsKafkaProducer;
import com.citydata.electricityservice.producer.TaskStatusKafkaProducer;
import com.citydata.electricityservice.repository.ElectricityUsageRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class ElectricityUsageConsumer {
    private final ElectricityUsageRepository electricityUsageRepository;
    private final ObjectMapper objectMapper;
    private final TaskStatusKafkaProducer taskStatusKafkaProducer;
    private final AnalyticsKafkaProducer analyticsKafkaProducer;

    @KafkaListener(topics = "#{${kafka.topics.electricity}}", groupId = "#{${kafka.consumer.group-id}}")
    public void consumeElectricityUsage(String message) {
        try {
            JsonNode jsonNode = objectMapper.readTree(message);

            String taskId = jsonNode.get("id").asText();
            JsonNode rawData = jsonNode.get("rawData");

            ElectricityUsage electricityUsage = ElectricityUsage.builder()
                    .taskId(taskId)
                    .customerId(rawData.get("customerId").asText())
                    .consumption(rawData.get("consumption").asDouble())
                    .billingCycle(rawData.get("billingCycle").asText())
                    .createdAt(Instant.now())
                    .build();

            electricityUsage = electricityUsageRepository.save(electricityUsage);
            log.debug("Electricity data saved: {}", electricityUsage);

            // 🔥 Produce Kafka event using TaskStatusEvent class
            TaskStatusEvent event = new TaskStatusEvent(taskId, TaskStatus.COMPLETED, Instant.now());
            taskStatusKafkaProducer.sendEvent(event);
            log.debug("Send task status event: {}", event);

            // Produce Kafka event to analytic
            AnalyticsEvent analyticsEvent = new AnalyticsEvent(ServiceType.WATER, electricityUsage.getConsumption(), Instant.now());
            analyticsKafkaProducer.sendEvent(analyticsEvent);
            log.debug("Send analytic event: {}", analyticsEvent);
        } catch (Exception e) {
            log.error("Error processing electricity data: {}", e.getMessage());
            /*
            Todo: Handle update task status failed.
             */
        }
    }
}

