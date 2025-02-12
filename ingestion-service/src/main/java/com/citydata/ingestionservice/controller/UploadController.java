package com.citydata.ingestionservice.controller;

import com.citydata.ingestionservice.constants.UploadStatus;
import com.citydata.ingestionservice.model.Upload;
import com.citydata.ingestionservice.producer.UploadKafkaProducer;
import com.citydata.ingestionservice.repository.UploadRepository;
import com.citydata.ingestionservice.storage.ObjectStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;

@Slf4j
@RestController
@RequestMapping("/uploads")
@RequiredArgsConstructor
public class UploadController {

    private final ObjectStorageService objectStorageService;
    private final UploadRepository uploadRepository;
    private final UploadKafkaProducer updateKafkaProducer;

    @PostMapping
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file,
                                             @RequestParam("userId") String userId) throws Exception {
        try {
            // Upload file to object storage
            String filePath = objectStorageService.uploadFile(file);

            // Save metadata to MongoDB
            Upload upload = new Upload();
            upload.setFileName(file.getOriginalFilename());
            upload.setFilePath(filePath);
            upload.setStatus(UploadStatus.PENDING);
            upload.setUploadedBy(userId);
            upload.setCreatedAt(Instant.now());
            upload.setUpdatedAt(Instant.now());

            Upload savedUpload = uploadRepository.save(upload);

            // Produce Kafka event
            updateKafkaProducer.sendEvent(savedUpload.getId());

            return ResponseEntity.ok("File uploaded successfully. ID: " + savedUpload.getId());
        } catch (Exception e) {
            log.error("Error occurred while uploading file", e);
        }
        return ResponseEntity.internalServerError().build();
    }
}
