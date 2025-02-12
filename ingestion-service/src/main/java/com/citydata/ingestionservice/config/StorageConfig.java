package com.citydata.ingestionservice.config;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.citydata.ingestionservice.storage.ObjectStorageService;
import com.citydata.ingestionservice.storage.S3StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfig {

    @Value("${storage.provider}")
    private String storageProvider;

    @Bean
    public ObjectStorageService objectStorageService(AmazonS3 s3Client) {
        if ("s3".equalsIgnoreCase(storageProvider)) {
            return new S3StorageService(s3Client);
        }
        throw new IllegalStateException("Unsupported storage provider: " + storageProvider);
    }

    @Bean
    public AmazonS3 amazonS3() {
        return AmazonS3ClientBuilder.standard().build();
    }
}

