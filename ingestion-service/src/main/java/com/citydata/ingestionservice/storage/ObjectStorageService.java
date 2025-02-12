package com.citydata.ingestionservice.storage;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;

public interface ObjectStorageService {
    String uploadFile(MultipartFile file) throws Exception;
    void deleteFile(String filePath);
    InputStream downloadFile(String filePath);
}

