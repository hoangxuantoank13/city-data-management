package com.citydata.wasteservice.consumer;

import com.citydata.wasteservice.constants.ServiceType;
import com.citydata.wasteservice.constants.TaskStatus;
import com.citydata.wasteservice.events.AnalyticsEvent;
import com.citydata.wasteservice.events.TaskStatusEvent;
import com.citydata.wasteservice.model.WasteUsage;
import com.citydata.wasteservice.producer.AnalyticsKafkaProducer;
import com.citydata.wasteservice.producer.TaskStatusKafkaProducer;
import com.citydata.wasteservice.repository.WasteUsageRepository;
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
public class WasteUsageConsumer {
    private final WasteUsageRepository wasteUsageRepository;
    private final ObjectMapper objectMapper;
    private final TaskStatusKafkaProducer taskStatusKafkaProducer;
    private final AnalyticsKafkaProducer analyticsKafkaProducer;

    @KafkaListener(topics = "#{${kafka.topics.waste}}", groupId = "#{${kafka.consumer.group-id}}")
    public void consumeWasteUsage(String message) {
        try {
            JsonNode jsonNode = objectMapper.readTree(message);

            String taskId = jsonNode.get("id").asText();
            JsonNode rawData = jsonNode.get("rawData");

            WasteUsage wasteUsage = WasteUsage.builder()
                    .taskId(taskId)
                    .customerId(rawData.get("customerId").asText())
                    .wasteAmount(rawData.get("wasteAmount").asLong())
                    .collectionDate(rawData.get("collectionDate").asText())
                    .createdAt(Instant.now())
                    .build();

            wasteUsage = wasteUsageRepository.save(wasteUsage);
            log.debug("Waste data saved: {}", wasteUsage);

            // 🔥 Produce Kafka event using TaskStatusEvent class
            TaskStatusEvent event = new TaskStatusEvent(taskId, TaskStatus.COMPLETED, Instant.now());
            taskStatusKafkaProducer.sendEvent(event);
            log.debug("Send task status event: {}", event);

            // Produce Kafka event to analytic
            AnalyticsEvent analyticsEvent = new AnalyticsEvent(ServiceType.WATER, wasteUsage.getWasteAmount(), Instant.now());
            analyticsKafkaProducer.sendEvent(analyticsEvent);
            log.debug("Send analytic event: {}", analyticsEvent);
        } catch (Exception e) {
            log.error("Error processing waste data: {}", e.getMessage());
            /*
            Todo: Handle update task status failed.
             */
        }
    }
}

