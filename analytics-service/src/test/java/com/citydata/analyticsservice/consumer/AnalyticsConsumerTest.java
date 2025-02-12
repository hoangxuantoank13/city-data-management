package com.citydata.analyticsservice.consumer;

import com.citydata.analyticsservice.constants.MetricName;
import com.citydata.analyticsservice.constants.ServiceType;
import com.citydata.analyticsservice.model.AggregatedData;
import com.citydata.analyticsservice.repository.AggregatedDataRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsConsumerTest {

    @Mock
    private AggregatedDataRepository aggregatedDataRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AnalyticsConsumer analyticsConsumer;

    private final String testMessage = "{\"serviceType\":\"WATER\",\"consumption\":100.5,\"timestamp\":\"2025-02-11T13:15:00Z\"}";

    @BeforeEach
    void setup() {
        reset(aggregatedDataRepository);
    }

    @Test
    void shouldUpdateTotalUsageMetric_WhenExistingDataPresent() throws Exception {
        // Arrange
        Instant eventTime = Instant.parse("2025-02-11T13:15:00Z");
        Instant timeBucket = eventTime.truncatedTo(ChronoUnit.HOURS);
        double newConsumption = 200.0;

        AggregatedData existingData = AggregatedData.builder()
                .serviceType(ServiceType.WATER)
                .metricName(MetricName.TOTAL_USAGE)
                .value(newConsumption)
                .timeBucket(timeBucket)
                .build();

        when(objectMapper.readTree(testMessage)).thenReturn(new ObjectMapper().readTree(testMessage));
        when(aggregatedDataRepository.findByServiceTypeAndMetricNameAndTimeBucket(ServiceType.WATER, MetricName.TOTAL_USAGE, timeBucket))
                .thenReturn(Optional.of(existingData));

        // Act
        analyticsConsumer.consumeAnalyticsEvent(testMessage);

        // Assert
        verify(aggregatedDataRepository).save(argThat(data ->
                data.getServiceType().equals(ServiceType.WATER) &&
                        data.getMetricName().equals(MetricName.TOTAL_USAGE) &&
                        data.getValue() == 300.5 && // 200.0 + 100.5
                        data.getTimeBucket().equals(timeBucket)
        ));
    }

    @Test
    void shouldCreateNewTotalUsageMetric_WhenNoExistingData() throws Exception {
        // Arrange
        Instant eventTime = Instant.parse("2025-02-11T13:15:00Z");
        Instant timeBucket = eventTime.truncatedTo(ChronoUnit.HOURS);
        double newConsumption = 100.5;

        when(objectMapper.readTree(testMessage)).thenReturn(new ObjectMapper().readTree(testMessage));
        when(aggregatedDataRepository.findByServiceTypeAndMetricNameAndTimeBucket(ServiceType.WATER, MetricName.TOTAL_USAGE, timeBucket))
                .thenReturn(Optional.empty());

        // Act
        analyticsConsumer.consumeAnalyticsEvent(testMessage);

        // Assert
        verify(aggregatedDataRepository).save(argThat(data ->
                data.getServiceType().equals(ServiceType.WATER) &&
                        data.getMetricName().equals(MetricName.TOTAL_USAGE) &&
                        data.getValue() == 100.5 &&
                        data.getTimeBucket().equals(timeBucket)
        ));
    }

    @Test
    void shouldHandleJsonProcessingError() throws Exception {
        // Arrange
        when(objectMapper.readTree(anyString())).thenThrow(new RuntimeException("JSON parse error"));

        // Act
        analyticsConsumer.consumeAnalyticsEvent(testMessage);

        // Assert
        verify(aggregatedDataRepository, never()).save(any());
    }
}
