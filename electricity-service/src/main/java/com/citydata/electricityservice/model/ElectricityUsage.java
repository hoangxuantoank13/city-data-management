package com.citydata.electricityservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "electricity_usage")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ElectricityUsage {
    @Id
    private String id;
    private String taskId;
    private String customerId;
    private double consumption; // in kWh
    private String billingCycle; // YYYY-MM format
    private Instant createdAt;
}
