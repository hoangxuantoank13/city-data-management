package com.citydata.waterservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "water_usage")
public class WaterUsage {
    @Id
    private String id;
    private String taskId;
    private String customerId;
    private double consumption;
    private String billingCycle;
    private Instant createdAt;
}

