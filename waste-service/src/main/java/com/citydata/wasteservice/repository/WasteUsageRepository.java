package com.citydata.wasteservice.repository;

import com.citydata.wasteservice.model.WasteUsage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WasteUsageRepository extends MongoRepository<WasteUsage, String> {
}

