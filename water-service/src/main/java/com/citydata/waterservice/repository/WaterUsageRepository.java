package com.citydata.waterservice.repository;

import com.citydata.waterservice.model.WaterUsage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WaterUsageRepository extends MongoRepository<WaterUsage, String> {
}

