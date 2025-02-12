package com.citydata.wasteservice.events;

import com.citydata.wasteservice.constants.ServiceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnalyticsEvent {
    private ServiceType serviceType;
    private double consumption;
    private Instant timestamp;
}

