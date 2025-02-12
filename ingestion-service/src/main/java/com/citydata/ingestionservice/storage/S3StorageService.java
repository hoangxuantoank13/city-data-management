package com.citydata.ingestionservice.storage;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.citydata.ingestionservice.storage.ObjectStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


@Service
@RequiredArgsConstructor
public class S3StorageService implements ObjectStorageService {

    private final AmazonS3 s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Override
    public String uploadFile(MultipartFile file) throws IOException {
        File tempFile = convertMultiPartToFile(file);
        String filePath = "uploads/" + file.getOriginalFilename();

        s3Client.putObject(new PutObjectRequest(bucketName, filePath, tempFile));
        tempFile.delete();

        return "s3://" + bucketName + "/" + filePath;
    }

    @Override
    public void deleteFile(String filePath) {
        String key = filePath.replace("s3://" + bucketName + "/", "");
        s3Client.deleteObject(new DeleteObjectRequest(bucketName, key));
    }

    @Override
    public InputStream downloadFile(String filePath) {
        String bucket = "city-storage";
        String key = filePath.replace("s3://" + bucket + "/", "");
        return s3Client.getObject(new GetObjectRequest(bucketName, key)).getObjectContent();
    }

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = File.createTempFile("temp", null);
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }
}
