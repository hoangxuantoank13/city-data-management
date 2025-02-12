package com.citydata.ingestionservice.controller;

import com.citydata.ingestionservice.constants.IngestionSource;
import com.citydata.ingestionservice.dto.WasteUsageData;
import com.citydata.ingestionservice.service.IngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ingestion/waste")
@RequiredArgsConstructor
@Slf4j
public class WasteIngestionController {
    private final IngestionService ingestionService;

    @PostMapping("/manual-entry")
    public ResponseEntity<String> handleManualEntry(@RequestBody WasteUsageData request) {
        log.info("Received manual entry request for WASTE: {}", request);
        try {
            ingestionService.processWasteUsageData(request, IngestionSource.ENTERED_MANUALLY, null);
            return ResponseEntity.ok("Waste data received successfully");
        } catch (Exception e) {
            log.error("Handle Manual Entry for WASTE data request failed {}", request, e);
        }
        return ResponseEntity.internalServerError().build();
    }
}
