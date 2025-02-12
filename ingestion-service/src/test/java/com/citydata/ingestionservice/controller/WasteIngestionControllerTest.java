package com.citydata.ingestionservice.controller;

import com.citydata.ingestionservice.constants.IngestionSource;
import com.citydata.ingestionservice.dto.WasteUsageData;
import com.citydata.ingestionservice.service.IngestionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WasteIngestionControllerTest {

    @Mock
    private IngestionService ingestionService;

    @InjectMocks
    private WasteIngestionController wasteIngestionController;

    @Test
    void testHandleManualEntry_Success() throws JsonProcessingException {
        // Given
        WasteUsageData request = new WasteUsageData();
        request.setCustomerId("123");
        request.setWasteAmount(45.6);
        request.setCollectionDate("2023-01");

        // When
        ResponseEntity<String> response = wasteIngestionController.handleManualEntry(request);

        // Then
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Waste data received successfully", response.getBody());

        verify(ingestionService).processWasteUsageData(request, IngestionSource.ENTERED_MANUALLY, null);
    }

    @Test
    void testHandleManualEntry_ExceptionThrown_ShouldReturnInternalServerError() throws JsonProcessingException {
        // Given
        WasteUsageData request = new WasteUsageData();
        request.setCustomerId("123");
        request.setWasteAmount(45.6);
        request.setCollectionDate("2023-01");

        doThrow(new RuntimeException("Processing error")).when(ingestionService)
                .processWasteUsageData(any(), any(), any());

        // When
        ResponseEntity<String> response = wasteIngestionController.handleManualEntry(request);

        // Then
        assertEquals(500, response.getStatusCodeValue());

        verify(ingestionService).processWasteUsageData(request, IngestionSource.ENTERED_MANUALLY, null);
    }
}
