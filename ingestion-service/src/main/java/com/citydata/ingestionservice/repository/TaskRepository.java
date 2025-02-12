package com.citydata.ingestionservice.repository;

import com.citydata.ingestionservice.model.Task;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends MongoRepository<Task, String> {
    List<Task> findByUploadId(String fileId);
}
