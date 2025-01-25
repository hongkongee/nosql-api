package com.example.mongodb.myapp.service;

import com.example.mongodb.myapp.model.PdfMetadata;
import com.example.mongodb.myapp.repository.PdfMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DocumentService {

    @Autowired
    private PdfMetadataRepository pdfMetadataRepository;

    public List<PdfMetadata> getAllMetadata() {
        return pdfMetadataRepository.findAll();
    }

    public Optional<PdfMetadata> getMetadataById(String id) {
        return pdfMetadataRepository.findById(id);
    }
}
