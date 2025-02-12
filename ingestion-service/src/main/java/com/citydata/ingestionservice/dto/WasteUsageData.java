package com.citydata.ingestionservice.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class WasteUsageData {
    private String customerId;
    private double wasteAmount; // in kg
    private String collectionDate; // YYYY-MM-DD format
}
