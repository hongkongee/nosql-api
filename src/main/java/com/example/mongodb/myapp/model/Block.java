package com.example.mongodb.myapp.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "blocks")
@Data
public class Block {
    @Id
    private String id; // 블록 고유 식별자
    private long documentId; // 문서 ID
    private int pageOrder; // 페이지 ID
    private int blockOrder; // 블록 순서
    private int categoryId; // 블록 카테고리
    private double bboxXMin; // 좌 상단 X 좌표
    private double bboxYMin; // 좌 상단 Y 좌표
    private double bboxXMax; // 우 하단 X 좌표
    private double bboxYMax; // 우 하단 Y 좌표
    private double confidenceScore; // 신뢰도 점수
    private String createdAt; // 생성 시간
    private List<Table> tables; // 테이블 정보 배열
    private List<Figure> figures; // 그림 정보 배열
    private List<Text> texts; // 텍스트 정보 배열
}

@Data
class Table {
    private String id; // 테이블 고유 식별자
    private String tableBlockId; // 테이블 블록 ID 참조
    private String tableCaptionBlockId; // 테이블 캡션 블록 ID 참조
    private String textCaption; // 테이블 캡션 텍스트
    private String tableData; // 구조화된 테이블 데이터
    private String createdAt; // 생성 시간

}

@Data
class Figure {
    private String id; // Figure 고유 식별자
    private String figureBlockId; // Figure 블록 ID 참조
    private String figureCaptionBlockId; // Figure 캡션 블록 ID 참조
    private String textCaption; // Figure 캡션 텍스트
    private String createdAt; // 생성 시간

}

@Data
class Text {
    private String id; // Text 고유 식별자
    private String blockId; // 블록 ID
    private String rawText; // 원본 텍스트
    private String cleanedText; // 정제된 텍스트
    private String createdAt; // 생성 시간

}
