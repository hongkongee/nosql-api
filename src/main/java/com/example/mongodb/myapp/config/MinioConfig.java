package com.example.mongodb.myapp.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import okhttp3.OkHttpClient;
import java.util.concurrent.TimeUnit;

@Configuration
public class MinioConfig {

    @Value("${minio.url}")
    private String minioUrl;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Bean
    public MinioClient minioClient() {
        // OkHttpClient 설정
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS) // 커넥션 타임아웃 설정
                .writeTimeout(10, TimeUnit.SECONDS)   // 쓰기 타임아웃 설정
                .readTimeout(10, TimeUnit.SECONDS)    // 읽기 타임아웃 설정
                .build();

        return MinioClient.builder()
                .endpoint(minioUrl) // MinIO 서버 URL
                .credentials(accessKey, secretKey) // 인증 정보
                .build();
    }
}
