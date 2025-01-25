package com.example.mongodb.myapp.controller;

import com.example.mongodb.myapp.model.PdfMetadata;
import com.example.mongodb.myapp.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pdf-metadata")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

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
}
