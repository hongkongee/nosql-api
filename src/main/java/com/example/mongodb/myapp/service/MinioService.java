package com.example.mongodb.myapp.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.UploadObjectArgs;
import io.minio.errors.*;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
public class MinioService {

    @Value("${minio.url}")
    private String minioUrl;

    private final MinioClient minioClient;

    @Autowired
    public MinioService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    public String uploadFile(String bucketName, String objectName, InputStream inputStream, long size) throws FileUploadException {

        try {
            if (size < 5 * 1024 * 1024) { // 5MiB 미만인 경우
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectName)
                                .stream(inputStream, size, -1)
                                .contentType("application/pdf") // 적절한 MIME 타입 설정
                                .build()
                );
            } else { // 5MiB 이상인 경우
                minioClient.uploadObject(
                        UploadObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectName)
                                .filename(objectName)
                                .build()
                );
            }

            // 업로드한 파일의 URL 생성
            String fileUrl = String.format("%s/%s/%s",
                    minioUrl, bucketName, objectName);
            System.out.println("Uploaded file URL: " + fileUrl);

            inputStream.close();
            return fileUrl;

        } catch (Exception e) {
            throw new FileUploadException("Failed to upload file: " + e.getMessage(), e);
        }



    }
}
