package com.example.mongodb.myapp.model;

import lombok.Data;

@Data
public class Status {
    private String id; // 상태 ID
    private String step; // 단계
    private String status; // 상태
    private String changedAt; // 상태 변경 시간
}
