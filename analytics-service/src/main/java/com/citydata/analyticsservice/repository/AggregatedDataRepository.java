package com.citydata.analyticsservice.repository;

import com.citydata.analyticsservice.constants.MetricName;
import com.citydata.analyticsservice.constants.ServiceType;
import com.citydata.analyticsservice.model.AggregatedData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface AggregatedDataRepository extends MongoRepository<AggregatedData, String> {
    Optional<AggregatedData> findByServiceTypeAndMetricNameAndTimeBucket(ServiceType serviceType, MetricName metricName, Instant timeBucket);
    List<AggregatedData> findByServiceTypeAndTimeBucketBetween(ServiceType serviceType, Instant start, Instant end);
}
