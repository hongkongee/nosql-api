package com.example.mongodb.myapp.service;

import com.example.mongodb.myapp.model.Page;
import com.example.mongodb.myapp.model.PdfMetadata;
import com.example.mongodb.myapp.repository.PdfMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DocumentService {

    @Autowired
    private PdfMetadataRepository pdfMetadataRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    public List<PdfMetadata> getAllMetadata() {
        return pdfMetadataRepository.findAll();
    }

    public Optional<PdfMetadata> getMetadataById(String id) {
        return pdfMetadataRepository.findById(id);
    }

    public PdfMetadata saveDocumentMetadata(PdfMetadata metadata) {
        return pdfMetadataRepository.save(metadata);
    }

    public void addPageToDocument(String documentId, Page newPage) {
        Query query = new Query(Criteria.where("_id").is(documentId));
        Update update = new Update().push("pages", newPage);
        mongoTemplate.updateFirst(query, update, PdfMetadata.class);
    }
}
