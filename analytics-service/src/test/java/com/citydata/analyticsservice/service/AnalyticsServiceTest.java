package com.citydata.analyticsservice.service;

import com.citydata.analyticsservice.constants.MetricName;
import com.citydata.analyticsservice.constants.ServiceType;
import com.citydata.analyticsservice.model.AggregatedData;
import com.citydata.analyticsservice.repository.AggregatedDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private AggregatedDataRepository aggregatedDataRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    private List<AggregatedData> mockData;
    private Instant startTime;
    private Instant endTime;

    @BeforeEach
    void setUp() {
        startTime = Instant.parse("2025-02-11T00:00:00Z");
        endTime = Instant.parse("2025-02-11T23:59:59Z");
        mockData = List.of(
                AggregatedData.builder()
                        .serviceType(ServiceType.WATER)
                        .metricName(MetricName.TOTAL_USAGE)
                        .value(500.0)
                        .timeBucket(startTime)
                        .build()
        );
    }

    @Test
    void getAggregatedData_shouldReturnAggregatedData() {
        when(aggregatedDataRepository.findByServiceTypeAndTimeBucketBetween(ServiceType.WATER, startTime, endTime))
                .thenReturn(mockData);

        List<AggregatedData> result = analyticsService.getAggregatedData(ServiceType.WATER, startTime, endTime);

        assertEquals(1, result.size());
        assertEquals(MetricName.TOTAL_USAGE, result.getFirst().getMetricName());
        assertEquals(500.0, result.getFirst().getValue());

        verify(aggregatedDataRepository, times(1))
                .findByServiceTypeAndTimeBucketBetween(ServiceType.WATER, startTime, endTime);
    }
}