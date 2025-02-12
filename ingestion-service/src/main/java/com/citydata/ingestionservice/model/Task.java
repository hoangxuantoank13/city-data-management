package com.citydata.ingestionservice.model;

import com.citydata.ingestionservice.constants.IngestionSource;
import com.citydata.ingestionservice.constants.ServiceType;
import com.citydata.ingestionservice.constants.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "tasks")
public class Task {
    @Id
    private String id;
    private IngestionSource source; // ENTERED_MANUALLY, UPLOAD, INTEGRATION
    private ServiceType serviceType; // WATER, ELECTRICITY, WASTE
    private String uploadId;
    private Map<String, Object> rawData;
    private TaskStatus status; // PENDING, PROCESSING,  COMPLETED, FAILED
    private Instant createdAt;
}

