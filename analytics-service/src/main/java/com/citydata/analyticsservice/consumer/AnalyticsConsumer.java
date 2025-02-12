package com.citydata.analyticsservice.consumer;

import com.citydata.analyticsservice.constants.MetricName;
import com.citydata.analyticsservice.constants.ServiceType;
import com.citydata.analyticsservice.model.AggregatedData;
import com.citydata.analyticsservice.repository.AggregatedDataRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsConsumer {
    private final AggregatedDataRepository aggregatedDataRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "#{${kafka.topics.analytics}}", groupId = "#{${kafka.consumer.group-id}}")
    public void consumeAnalyticsEvent(String message) {
        try {
            JsonNode jsonNode = objectMapper.readTree(message);

            ServiceType serviceType = ServiceType.valueOf(jsonNode.get("serviceType").asText());
            double consumption = jsonNode.get("consumption").asDouble();
            Instant eventTime = Instant.parse(jsonNode.get("timestamp").asText());

            // Compute hourly time bucket
            Instant timeBucket = eventTime.truncatedTo(ChronoUnit.HOURS);

            // Update total usage per hour
            updateTotalUsageMetric(serviceType, consumption, timeBucket);

        } catch (Exception e) {
            log.error("Error processing analytics event: {}", e.getMessage());
        }
    }

    private void updateTotalUsageMetric(ServiceType serviceType, double value, Instant timeBucket) {
        Optional<AggregatedData> existingData = aggregatedDataRepository.findByServiceTypeAndMetricNameAndTimeBucket(
                serviceType, MetricName.TOTAL_USAGE, timeBucket);

        double updatedValue = value;
        if (existingData.isPresent()) {
            AggregatedData data = existingData.get();
            updatedValue += data.getValue(); // Accumulate total
            data.setValue(updatedValue);
            aggregatedDataRepository.save(data);
        } else {
            AggregatedData newData = AggregatedData.builder()
                    .serviceType(serviceType)
                    .metricName(MetricName.TOTAL_USAGE)
                    .value(updatedValue)
                    .timeBucket(timeBucket)
                    .build();
            aggregatedDataRepository.save(newData);
        }
        log.debug("Updated metric: {} - {} = {} at {}", serviceType, MetricName.TOTAL_USAGE, updatedValue, timeBucket);
    }
}
