package com.example.mongodb.myapp.controller;

import com.example.mongodb.myapp.service.DataService;
import com.fasterxml.jackson.databind.JsonNode;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/data")
public class DataController {

    private final DataService dataService;
    @Autowired
    public DataController(DataService dataService) {
        this.dataService = dataService;
    }

    // 데이터 넣기
    @PostMapping("/insert-documents")
    public ResponseEntity<String> insertDocuments(@RequestParam String collectionName, @RequestBody String jsonArrayString) {
        String response = null;
        try {
            response = dataService.insertDocuments(collectionName, jsonArrayString);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // 데이터 조회
    @PostMapping("/find-documents")
    public ResponseEntity<String> searchDocumentes(@RequestParam String collectionName, @RequestBody JsonNode requestBody) {
        String response = null;
        try {
            JsonNode criteriaJson = requestBody.get("criteria");
            JsonNode projectionJson = requestBody.get("projection");
            JsonNode sortJson = requestBody.get("sort");

            // DocumentService에 검색 조건 전달
            List<Document> documents = dataService.findDocuments(criteriaJson, projectionJson, sortJson, collectionName);
            return ResponseEntity.ok(documents.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 데이터 수정
    @PostMapping("/update-documents")
    public ResponseEntity<String> updateDocuments(@RequestParam String collectionName, @RequestBody JsonNode requestBody) {

        try {
            JsonNode filterJson = requestBody.get("filter");
            JsonNode updateJson = requestBody.get("update");
            Boolean isUpsert = requestBody.get("upsert").asBoolean();
            String result = dataService.updateDocuments(filterJson, updateJson, isUpsert, collectionName);
            return new ResponseEntity<>(result, HttpStatus.CREATED);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/delete-documents")
    public ResponseEntity<String> deleteDocuments(@RequestParam String collectionName, @RequestBody JsonNode requestBody) {

        try {
            JsonNode filterJson = requestBody.get("filter");
            String result = dataService.deleteDocuments(filterJson, collectionName);
            return new ResponseEntity<>(result, HttpStatus.CREATED);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
