package com.example.mongodb.myapp.model;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Data
@Document(collection = "pdf_metadata")
public class PdfMetadata {

    @Id
    private String id;
    private String userId;
    private String fileHash;
    private String fileName;
    private String filePath; // MinIO 경로
    private long fileSize;
    private String fileFormat;
    private int totalPages;
    private String createdAt; // 문서 생성 시간
    private String startedAt; // 문서 프로세스 시작 시각
    private String completedAt; // 문서 프로세스 완료 시각
    private long processTimeSeconds; // 문서 프로세스 진행 시간

    private List<Status> statuses;
    private List<Page> pages; // 이미 정의된 페이지 배열
    private TextFullSummary textFullSummary;


}
