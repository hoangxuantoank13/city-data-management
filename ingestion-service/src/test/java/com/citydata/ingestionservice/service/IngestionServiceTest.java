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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IngestionServiceTest {

    @Mock
    private WasteKafkaProducer wasteKafkaProducer;

    @Mock
    private ElectricityKafkaProducer electricityKafkaProducer;

    @Mock
    private WaterKafkaProducer waterKafkaProducer;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private IngestionService ingestionService;

    @Test
    void processWasteUsageData_Success() throws JsonProcessingException {
        // Given
        WasteUsageData data = new WasteUsageData();
        data.setCustomerId("123");
        data.setWasteAmount(50.0);
        data.setCollectionDate("2024-01-01");

        Task mockTask = Task.builder()
                .source(IngestionSource.UPLOAD)
                .serviceType(ServiceType.WASTE)
                .uploadId("upload123")
                .rawData(Map.of("customerId", "123", "wasteAmount", 50.0, "collectionDate", "2024-01-01"))
                .status(TaskStatus.PENDING)
                .createdAt(Instant.now())
                .build();

        when(taskRepository.save(any(Task.class))).thenReturn(mockTask);

        // When
        ingestionService.processWasteUsageData(data, IngestionSource.UPLOAD, "upload123");

        // Then
        verify(taskRepository).save(any(Task.class));
        verify(wasteKafkaProducer).sendEvent(any(Task.class));
    }

    @Test
    void processElectricityUsageData_Success() throws JsonProcessingException {
        // Given
        ElectricityUsageData data = new ElectricityUsageData();
        data.setCustomerId("456");
        data.setConsumption(100.5);
        data.setBillingCycle("2023-12");

        Task mockTask = Task.builder()
                .source(IngestionSource.INTEGRATION)
                .serviceType(ServiceType.ELECTRICITY)
                .uploadId(null)
                .rawData(Map.of("customerId", "456", "consumption", 100.5, "billingCycle", "2023-12"))
                .status(TaskStatus.PENDING)
                .createdAt(Instant.now())
                .build();

        when(taskRepository.save(any(Task.class))).thenReturn(mockTask);

        // When
        ingestionService.processElectricityUsageData(data, IngestionSource.INTEGRATION, null);

        // Then
        verify(taskRepository).save(any(Task.class));
        verify(electricityKafkaProducer).sendEvent(any(Task.class));
    }

    @Test
    void processWaterUsageData_Success() throws JsonProcessingException {
        // Given
        WaterUsageData data = new WaterUsageData();
        data.setCustomerId("789");
        data.setConsumption(75.3);
        data.setBillingCycle("2024-02");

        Task mockTask = Task.builder()
                .source(IngestionSource.ENTERED_MANUALLY)
                .serviceType(ServiceType.WATER)
                .uploadId(null)
                .rawData(Map.of("customerId", "789", "consumption", 75.3, "billingCycle", "2024-02"))
                .status(TaskStatus.PENDING)
                .createdAt(Instant.now())
                .build();

        when(taskRepository.save(any(Task.class))).thenReturn(mockTask);

        // When
        ingestionService.processWaterUsageData(data, IngestionSource.ENTERED_MANUALLY, null);

        // Then
        verify(taskRepository).save(any(Task.class));
        verify(waterKafkaProducer).sendEvent(any(Task.class));
    }
}
