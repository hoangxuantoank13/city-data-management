package com.citydata.ingestionservice.service;

import com.citydata.ingestionservice.constants.IngestionSource;
import com.citydata.ingestionservice.constants.ServiceType;
import com.citydata.ingestionservice.constants.TaskStatus;
import com.citydata.ingestionservice.dto.ElectricityUsageData;
import com.citydata.ingestionservice.dto.WasteUsageData;
import com.citydata.ingestionservice.dto.WaterUsageData;
import com.citydata.ingestionservice.model.Task;
import com.citydata.ingestionservice.producer.ElectricityKafkaProducer;
import com.citydata.ingestionservice.producer.WasteKafkaProducer;
import com.citydata.ingestionservice.producer.WaterKafkaProducer;
import com.citydata.ingestionservice.repository.TaskRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.lang.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class IngestionService {
    private final WaterKafkaProducer waterKafkaProducer;
    private final WasteKafkaProducer wasteKafkaProducer;
    private final ElectricityKafkaProducer electricityKafkaProducer;
    private final TaskRepository taskRepository;

    public void processWasteUsageData(WasteUsageData data, IngestionSource source, @Nullable String uploadId) throws JsonProcessingException {
        Map<String, Object> rawData = new HashMap<>();
        rawData.put("customerId", data.getCustomerId());
        rawData.put("wasteAmount", data.getWasteAmount());
        rawData.put("collectionDate", data.getCollectionDate());
        var task = Task.builder()
                .source(source)
                .serviceType(ServiceType.WASTE)
                .uploadId(uploadId)
                .rawData(rawData)
                .status(TaskStatus.PENDING)
                .createdAt(Instant.now())
                .build();
        task = taskRepository.save(task);

        // Produce event to Kafka
        wasteKafkaProducer.sendEvent(task);
        log.info("Produced waste task: {}", task);
    }

    public void processElectricityUsageData(ElectricityUsageData data, IngestionSource source, @Nullable String uploadId) throws JsonProcessingException {
        Map<String, Object> rawData = new HashMap<>();
        rawData.put("customerId", data.getCustomerId());
        rawData.put("consumption", data.getConsumption());
        rawData.put("billingCycle", data.getBillingCycle());
        var task = Task.builder()
                .source(source)
                .serviceType(ServiceType.ELECTRICITY)
                .uploadId(uploadId)
                .rawData(rawData)
                .status(TaskStatus.PENDING)
                .createdAt(Instant.now())
                .build();
        task = taskRepository.save(task);

        // Produce event to Kafka
        electricityKafkaProducer.sendEvent(task);
        log.info("Produced electricity task: {}", task);
    }

    public void processWaterUsageData(WaterUsageData data, IngestionSource source, @Nullable String uploadId) throws JsonProcessingException {
        Map<String, Object> rawData = new HashMap<>();
        rawData.put("customerId", data.getCustomerId());
        rawData.put("consumption", data.getConsumption());
        rawData.put("billingCycle", data.getBillingCycle());
        var task = Task.builder()
                .source(source)
                .serviceType(ServiceType.WATER)
                .uploadId(uploadId)
                .rawData(rawData)
                .status(TaskStatus.PENDING)
                .createdAt(Instant.now())
                .build();
        task = taskRepository.save(task);

        // Produce event to Kafka
        waterKafkaProducer.sendEvent(task);
        log.info("Produced water task: {}", task);
    }
}
