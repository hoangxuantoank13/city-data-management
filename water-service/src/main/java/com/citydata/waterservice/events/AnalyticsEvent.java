package com.citydata.waterservice.events;

import com.citydata.waterservice.constants.ServiceType;
import com.citydata.waterservice.constants.TaskStatus;
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

