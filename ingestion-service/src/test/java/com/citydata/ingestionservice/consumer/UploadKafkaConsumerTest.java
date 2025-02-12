package com.citydata.ingestionservice.consumer;

import com.citydata.ingestionservice.constants.IngestionSource;
import com.citydata.ingestionservice.constants.ServiceType;
import com.citydata.ingestionservice.constants.UploadStatus;
import com.citydata.ingestionservice.model.Upload;
import com.citydata.ingestionservice.parser.csv.ElectricityCsvParser;
import com.citydata.ingestionservice.parser.csv.WasteCsvParser;
import com.citydata.ingestionservice.parser.csv.WaterCsvParser;
import com.citydata.ingestionservice.repository.UploadRepository;
import com.citydata.ingestionservice.service.IngestionService;
import com.citydata.ingestionservice.storage.ObjectStorageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.SneakyThrows;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UploadKafkaConsumerTest {

    @Mock
    private UploadRepository uploadRepository;

    @Mock
    private ObjectStorageService objectStorageService;

    @Mock
    private WaterCsvParser waterCsvParser;

    @Mock
    private ElectricityCsvParser electricityCsvParser;

    @Mock
    private WasteCsvParser wasteCsvParser;

    @Mock
    private IngestionService ingestionService;

    @InjectMocks
    private UploadKafkaConsumer uploadKafkaConsumer;

    private Upload upload;

    @BeforeEach
    void setUp() {
        upload = new Upload();
        upload.setId("upload123");
        upload.setFileName("test.csv");
        upload.setFilePath("uploads/test.csv");
        upload.setStatus(UploadStatus.PENDING);
        upload.setUpdatedAt(Instant.now());
    }

    @Test
    void testConsume_ValidUploadFile_ShouldProcessSuccessfully() throws JsonProcessingException {
        String csvContent = "service_type,customerId,consumption,billingCycle,timestamp\n" +
                "ELECTRICITY,123,456.7,2023,2024";

        ByteArrayInputStream fileStream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));
        ConsumerRecord<String, String> record = new ConsumerRecord<>("test-topic", 0, 0L, "key", "upload123");

        when(uploadRepository.findById("upload123")).thenReturn(Optional.of(upload));
        when(objectStorageService.downloadFile("uploads/test.csv")).thenReturn(fileStream);

        uploadKafkaConsumer.consume(record);

        verify(uploadRepository).save(upload);
        verify(electricityCsvParser).parse(any());
        verify(ingestionService).processElectricityUsageData(any(), eq(IngestionSource.UPLOAD), eq("upload123"));
    }

    @Test
    void testConsume_InvalidUploadId_ShouldLogError() {
        ConsumerRecord<String, String> record = new ConsumerRecord<>("test-topic", 0, 0L, "key", "invalidUploadId");

        when(uploadRepository.findById("invalidUploadId")).thenReturn(Optional.empty());

        uploadKafkaConsumer.consume(record);

        verify(uploadRepository, never()).save(any());
        verify(objectStorageService, never()).downloadFile(any());
        verify(waterCsvParser, never()).parse(any());
        verify(electricityCsvParser, never()).parse(any());
        verify(wasteCsvParser, never()).parse(any());
    }

    @Test
    void testConsume_InvalidCsvFormat_ShouldLogError() throws JsonProcessingException {
        String csvContent = "invalid_header1,invalid_header2\n" + "value1,value2";
        ByteArrayInputStream fileStream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));
        ConsumerRecord<String, String> record = new ConsumerRecord<>("test-topic", 0, 0L, "key", "upload123");

        when(uploadRepository.findById("upload123")).thenReturn(Optional.of(upload));
        when(objectStorageService.downloadFile("uploads/test.csv")).thenReturn(fileStream);

        uploadKafkaConsumer.consume(record);

        verify(uploadRepository).save(upload);
        verify(waterCsvParser, never()).parse(any());
        verify(electricityCsvParser, never()).parse(any());
        verify(wasteCsvParser, never()).parse(any());
        verify(ingestionService, never()).processElectricityUsageData(any(), any(), any());
    }
}
