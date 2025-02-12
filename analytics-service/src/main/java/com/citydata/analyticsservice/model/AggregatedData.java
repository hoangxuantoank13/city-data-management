package com.citydata.analyticsservice.model;

import com.citydata.analyticsservice.constants.MetricName;
import com.citydata.analyticsservice.constants.ServiceType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "aggregated_data")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AggregatedData {
    @Id
    private String id;
    private ServiceType serviceType; // WATER, ELECTRICITY, WASTE
    private MetricName metricName;  // TOTAL_USAGE, AVERAGE_USAGE
    private double value;       // Aggregated value
    private Instant timeBucket; // Time-based aggregation (e.g., hourly)
}
