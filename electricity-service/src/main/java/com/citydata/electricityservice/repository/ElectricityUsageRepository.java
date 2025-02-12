package com.citydata.electricityservice.repository;

import com.citydata.electricityservice.model.ElectricityUsage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElectricityUsageRepository extends MongoRepository<ElectricityUsage, String> {
}

