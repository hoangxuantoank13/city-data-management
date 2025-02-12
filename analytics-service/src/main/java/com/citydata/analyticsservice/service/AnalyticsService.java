package com.citydata.analyticsservice.service;

import com.citydata.analyticsservice.constants.ServiceType;
import com.citydata.analyticsservice.model.AggregatedData;
import com.citydata.analyticsservice.repository.AggregatedDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {
    private final AggregatedDataRepository aggregatedDataRepository;

    public List<AggregatedData> getAggregatedData(ServiceType serviceType, Instant startTime, Instant endTime) {
        return aggregatedDataRepository.findByServiceTypeAndTimeBucketBetween(serviceType, startTime, endTime);
    }
}
