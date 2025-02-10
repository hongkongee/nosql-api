package com.example.mongodb.myapp.controller;

import com.example.mongodb.myapp.service.CollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/api/collection")
public class CollectionController {

    private final CollectionService collectionService;
    @Autowired
    public CollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    // 컬렉션 생성
    @PostMapping
    public ResponseEntity<String> createCollection(@RequestParam String collectionName) {
        String response = collectionService.createCollection(collectionName);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // 컬렉션 목록 조회
    @GetMapping
    public ResponseEntity<ArrayList<String>> getAllCollections() {
        ArrayList<String> collections = collectionService.getAllCollections();
        return ResponseEntity.ok(collections);
    }

    // 컬렉션 삭제
    @DeleteMapping
    public ResponseEntity<String> deleteCollection(@RequestParam String collectionName) {
        String response = collectionService.deleteCollection(collectionName);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // 컬렉션 초기화
    @DeleteMapping("truncate")
    public ResponseEntity<String> truncateCollection(@RequestParam String collectionName) {
        String response = collectionService.truncateCollection(collectionName);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // 컬렉션 정보 조회
//    @GetMapping("info")
//    public ResponseEntity<ArrayList<String>> getCollectionMetadata(@RequestParam String collectionName) {
//        collectionService.getCollectionMetadata(collectionName);
//        return ResponseEntity.ok(collections);
//    }
}
