package com.example.mongodb.myapp.model;

import lombok.Data;

@Data
public class Page {
    private String id; // 페이지 ID
    private int pageOrder; // 페이지 순서
    private int pageWidth; // 페이지 너비 (포인트 단위)
    private int pageHeight; // 페이지 높이 (포인트 단위)
    private int columnsPerPage; // 페이지당 열 수
}
