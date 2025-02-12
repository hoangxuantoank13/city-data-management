package com.citydata.ingestionservice.model;

import com.citydata.ingestionservice.constants.UploadStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "uploads")
public class Upload {
    @Id
    private String id;
    private String fileName;
    private String filePath;
    private UploadStatus status; // PENDING / PROCESSING / COMPLETED / FAILED
    private String uploadedBy;
    private Instant createdAt;
    private Instant updatedAt;
}

