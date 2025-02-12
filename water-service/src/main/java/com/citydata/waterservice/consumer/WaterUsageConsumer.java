package com.citydata.waterservice.consumer;

import com.citydata.waterservice.constants.ServiceType;
import com.citydata.waterservice.constants.TaskStatus;
import com.citydata.waterservice.events.AnalyticsEvent;
import com.citydata.waterservice.events.TaskStatusEvent;
import com.citydata.waterservice.model.WaterUsage;
import com.citydata.waterservice.producer.AnalyticsKafkaProducer;
import com.citydata.waterservice.producer.TaskStatusKafkaProducer;
import com.citydata.waterservice.repository.WaterUsageRepository;
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
public class WaterUsageConsumer {
    private final WaterUsageRepository waterUsageRepository;
    private final ObjectMapper objectMapper;
    private final TaskStatusKafkaProducer taskStatusKafkaProducer;
    private final AnalyticsKafkaProducer analyticsKafkaProducer;

    @KafkaListener(topics = "#{${kafka.topics.water}}", groupId = "#{${kafka.consumer.group-id}}")
    public void consumeWaterUsage(String message) {
        try {
            JsonNode jsonNode = objectMapper.readTree(message);

            String taskId = jsonNode.get("id").asText();
            JsonNode rawData = jsonNode.get("rawData");

            WaterUsage waterUsage = WaterUsage.builder()
                    .taskId(taskId)
                    .customerId(rawData.get("customerId").asText())
                    .consumption(rawData.get("consumption").asDouble())
                    .billingCycle(rawData.get("billingCycle").asText())
                    .createdAt(Instant.now())
                    .build();

            waterUsage = waterUsageRepository.save(waterUsage);
            log.debug("Water data saved: {}", waterUsage);

            // 🔥 Produce Kafka event using TaskStatusEvent class
            TaskStatusEvent event = new TaskStatusEvent(taskId, TaskStatus.COMPLETED, Instant.now());
            taskStatusKafkaProducer.sendEvent(event);
            log.debug("Send task status event: {}", event);

            // Produce Kafka event to analytic
            AnalyticsEvent analyticsEvent = new AnalyticsEvent(ServiceType.WATER, waterUsage.getConsumption(), Instant.now());
            analyticsKafkaProducer.sendEvent(analyticsEvent);
            log.debug("Send analytic event: {}", analyticsEvent);
        } catch (Exception e) {
            log.error("Error processing water data: {}", e.getMessage());
            /*
            Todo: Handle update task status failed.
             */
        }
    }
}

