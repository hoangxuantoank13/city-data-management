package com.citydata.evnintegrationgatewayservice.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ElectricityUsageEvent {
    private String customerId;
    private double consumption;
    private String billingCycle;
}

