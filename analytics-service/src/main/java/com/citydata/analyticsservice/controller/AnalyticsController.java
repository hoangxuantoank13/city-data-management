package com.citydata.analyticsservice.controller;

import com.citydata.analyticsservice.constants.ServiceType;
import com.citydata.analyticsservice.model.AggregatedData;
import com.citydata.analyticsservice.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    @GetMapping("/{serviceType}")
    public ResponseEntity<List<AggregatedData>> getAggregatedData(
            @PathVariable String serviceType,
            @RequestParam Instant startTime,
            @RequestParam Instant endTime) {

        List<AggregatedData> data = analyticsService.getAggregatedData(ServiceType.valueOf(serviceType), startTime, endTime);
        return ResponseEntity.ok(data);
    }
}
