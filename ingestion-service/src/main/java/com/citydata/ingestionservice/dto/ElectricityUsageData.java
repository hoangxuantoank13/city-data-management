package com.citydata.ingestionservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ElectricityUsageData {
    private String customerId;
    private double consumption;
    private String billingCycle;
}

