package com.citydata.ingestionservice.controller;

import com.citydata.ingestionservice.constants.UploadStatus;
import com.citydata.ingestionservice.model.Upload;
import com.citydata.ingestionservice.producer.UploadKafkaProducer;
import com.citydata.ingestionservice.repository.UploadRepository;
import com.citydata.ingestionservice.storage.ObjectStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UploadControllerTest {

    @Mock
    private ObjectStorageService objectStorageService;

    @Mock
    private UploadRepository uploadRepository;

    @Mock
    private UploadKafkaProducer uploadKafkaProducer;

    @InjectMocks
    private UploadController uploadController;

    private MockMultipartFile mockFile;
    private Upload savedUpload;

    @BeforeEach
    void setUp() {
        mockFile = new MockMultipartFile("file", "test.csv", "text/csv", "dummy data".getBytes());

        savedUpload = new Upload();
        savedUpload.setId("upload123");
        savedUpload.setFileName("test.csv");
        savedUpload.setFilePath("s3://bucket/test.csv");
        savedUpload.setStatus(UploadStatus.PENDING);
        savedUpload.setUploadedBy("user123");
        savedUpload.setCreatedAt(Instant.now());
        savedUpload.setUpdatedAt(Instant.now());
    }

    @Test
    void testUploadFile_SuccessfulUpload() throws Exception {
        when(objectStorageService.uploadFile(mockFile)).thenReturn("s3://bucket/test.csv");
        when(uploadRepository.save(any())).thenReturn(savedUpload);

        ResponseEntity<String> response = uploadController.uploadFile(mockFile, "user123");

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("File uploaded successfully"));

        verify(objectStorageService).uploadFile(mockFile);
        verify(uploadRepository).save(any(Upload.class));
        verify(uploadKafkaProducer).sendEvent(savedUpload.getId());
    }

    @Test
    void testUploadFile_ExceptionThrown_ShouldReturnInternalServerError() throws Exception {
        when(objectStorageService.uploadFile(mockFile)).thenThrow(new RuntimeException("Storage error"));

        ResponseEntity<String> response = uploadController.uploadFile(mockFile, "user123");

        assertEquals(500, response.getStatusCodeValue());

        verify(objectStorageService).uploadFile(mockFile);
        verify(uploadRepository, never()).save(any());
        verify(uploadKafkaProducer, never()).sendEvent(any());
    }
}
