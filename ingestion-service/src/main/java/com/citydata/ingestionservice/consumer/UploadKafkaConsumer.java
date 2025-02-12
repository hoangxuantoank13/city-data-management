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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadKafkaConsumer {
    private final UploadRepository uploadRepository;
    private final ObjectStorageService objectStorageService;
    private final WaterCsvParser waterCsvParser;
    private final ElectricityCsvParser electricityCsvParser;
    private final WasteCsvParser wasteCsvParser;
    private final IngestionService ingestionService;

    @KafkaListener(topics = "#{${kafka.topics.uploads}}", groupId = "#{${kafka.consumer.group-id}}")
    public void consume(ConsumerRecord<String, String> record) {
        try {
            log.info("Received message: {}", record.value());
            String uploadId = record.value();

            // Fetch file metadata
            Upload upload = uploadRepository.findById(uploadId)
                    .orElseThrow(() -> new RuntimeException("File metadata not found: " + uploadId));
            // Update upload status to PENDING
            upload.setStatus(UploadStatus.PENDING);
            upload.setUpdatedAt(Instant.now());
            uploadRepository.save(upload);

            log.info("Processing file: {}", upload.getFileName());

            // Download file from object storage (S3, MinIO, etc.)
            InputStream fileStream = objectStorageService.downloadFile(upload.getFilePath());

            // Process file content
            if (upload.getFileName().endsWith(".csv")) {
                processCsv(fileStream, upload);
            } else {
                throw new IllegalArgumentException("Unsupported file format: " + upload.getFileName());
            }
            log.info("File processing completed for: {}", upload.getFileName());
        } catch (Exception e) {
            log.error("Error processing file", e);
            /* Todo: Handle case process file failed
            - Update status
            - Retry
            - Make sure file are processed idempotency
             */
        }
    }

    private void processCsv(InputStream fileStream, Upload upload) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(fileStream));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            for (CSVRecord record : csvParser) {
                try {
                    var serviceType = ServiceType.valueOf(record.get("service_type"));
                    switch (serviceType) {
                        case WASTE:
                            var wasteUsageData = wasteCsvParser.parse(record);
                            ingestionService.processWasteUsageData(wasteUsageData, IngestionSource.UPLOAD, upload.getId());
                            break;
                        case ELECTRICITY:
                            var electricityUsageData = electricityCsvParser.parse(record);
                            ingestionService.processElectricityUsageData(electricityUsageData, IngestionSource.UPLOAD, upload.getId());
                            break;
                        case WATER:
                            var waterUsageData = waterCsvParser.parse(record);
                            ingestionService.processWaterUsageData(waterUsageData, IngestionSource.UPLOAD, upload.getId());
                            break;
                    }

                } catch (Exception e) {
                    log.error("Error processing row {}", record, e);
                }
            }
        } catch (Exception e) {
            log.error("Error processing file {}", upload.getFileName(), e);
        }
    }
}
