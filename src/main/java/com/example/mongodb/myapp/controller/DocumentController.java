package com.example.mongodb.myapp.controller;

import com.example.mongodb.myapp.model.PdfMetadata;
import com.example.mongodb.myapp.service.DocumentService;
import com.example.mongodb.myapp.service.MinioService;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/pdf-metadata")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private MinioService minioService;

    // 모든 문서 조회
    @GetMapping
    public List<PdfMetadata> getAllMetadata() {
        return documentService.getAllMetadata();
    }

    // id로 문서 찾기
    @GetMapping("/{id}")
    public ResponseEntity<PdfMetadata> getMetadataById(@PathVariable String id) {
        return documentService.getMetadataById(id)
                .map(ResponseEntity::ok)
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // pdf 파일 업로드
    @PostMapping("/upload")
    public ResponseEntity<String> uploadDocument(@RequestParam("file") MultipartFile file,
                                                 @RequestParam("userId") String userId) throws FileUploadException {

        try {
            // MinIO에 파일 업로드 (추후 날짜에 따른 파티셔닝 필요)
            String bucketName = "pdf-bucket";
            String objectName = file.getOriginalFilename();

            String filePath = minioService.uploadFile(bucketName, objectName, file.getInputStream(), file.getSize());

            // 메타데이터 저장
            PdfMetadata metadata = new PdfMetadata();
            metadata.setUserId(userId);
            metadata.setFileName(objectName);
            metadata.setFilePath(filePath); // MinIO 경로
            metadata.setFileSize(file.getSize());
            metadata.setFileFormat("pdf");

            // 현재 시간 설정 (UTC)
            String currentTime = Instant.now().toString(); // ISO 8601 형식
            metadata.setCreatedAt(currentTime); // 현재 시간으로 설정 가능

            PdfMetadata pdfPath = documentService.saveDocumentMetadata(metadata);

            return ResponseEntity.ok("File uploaded successfully." + pdfPath);

        } catch (IOException e) {
            throw new FileUploadException("Failed to read file: " + e.getMessage(), e);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("File upload failed: " + e.getMessage());
        }

    }
}
