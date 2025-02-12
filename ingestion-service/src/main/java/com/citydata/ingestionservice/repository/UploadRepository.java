package com.citydata.ingestionservice.repository;

import com.citydata.ingestionservice.model.Upload;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UploadRepository extends MongoRepository<Upload, String> {
}
