package com.example.mongodb.myapp.model;

import lombok.Data;

@Data
public class TextFullSummary {
    private String id; // 요약 ID
    private String summaryText; // 요약 텍스트
    private String createdAt; // 요약 생성일
}
