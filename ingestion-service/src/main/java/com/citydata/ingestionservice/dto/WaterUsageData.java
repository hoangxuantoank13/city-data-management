package com.citydata.ingestionservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WaterUsageData {
    private String customerId;
    private double consumption;
    private String billingCycle;
}

