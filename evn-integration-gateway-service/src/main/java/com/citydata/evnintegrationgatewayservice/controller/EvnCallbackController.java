package com.citydata.evnintegrationgatewayservice.controller;

import com.citydata.evnintegrationgatewayservice.events.ElectricityUsageEvent;
import com.citydata.evnintegrationgatewayservice.service.EvnCallbackService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/evn")
@RequiredArgsConstructor
@Slf4j
public class EvnCallbackController {
    private final EvnCallbackService evnCallbackService;

    @PostMapping("/callback")
    public ResponseEntity<String> receiveCallback(@RequestBody ElectricityUsageEvent event) {
        log.info("Received EVN callback: {}", event);
        try {
            evnCallbackService.processCallback(event);
            return ResponseEntity.ok("Received");
        } catch (JsonProcessingException e) {
            log.error("Error when receiveCallback {}", event,e);
        }
        return ResponseEntity.internalServerError().build();
    }
}

