package com.citydata.wasteservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "waste_usage")
public class WasteUsage {
    @Id
    private String id;
    private String taskId;
    private String customerId;
    private double wasteAmount; // in kg
    private String collectionDate; // YYYY-MM-DD format
    private Instant createdAt;
}

