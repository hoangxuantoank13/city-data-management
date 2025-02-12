package com.citydata.analyticsservice.controller;

import com.citydata.analyticsservice.constants.MetricName;
import com.citydata.analyticsservice.constants.ServiceType;
import com.citydata.analyticsservice.model.AggregatedData;
import com.citydata.analyticsservice.service.AnalyticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class AnalyticsControllerTest {

    @Mock
    private AnalyticsService analyticsService;

    @InjectMocks
    private AnalyticsController analyticsController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAggregatedData() {
        // Arrange
        ServiceType serviceType = ServiceType.WATER;
        Instant startTime = Instant.parse("2025-02-11T00:00:00Z");
        Instant endTime = Instant.parse("2025-02-11T23:59:59Z");

        List<AggregatedData> expectedData = List.of(
                AggregatedData.builder()
                        .serviceType(serviceType)
                        .metricName(MetricName.TOTAL_USAGE)
                        .value(500.0)
                        .timeBucket(startTime)
                        .build()
        );

        when(analyticsService.getAggregatedData(serviceType, startTime, endTime)).thenReturn(expectedData);

        // Act
        ResponseEntity<List<AggregatedData>> response = analyticsController.getAggregatedData(serviceType.name(), startTime, endTime);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expectedData, response.getBody());
    }
}
