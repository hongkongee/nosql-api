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
}
