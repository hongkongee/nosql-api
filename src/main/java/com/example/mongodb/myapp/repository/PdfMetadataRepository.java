package com.example.mongodb.myapp.repository;

import com.example.mongodb.myapp.model.PdfMetadata;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PdfMetadataRepository extends MongoRepository<PdfMetadata, String> {
}
