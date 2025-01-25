package com.example.mongodb.myapp.model;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document(collection = "pdf_metadata")
public class PdfMetadata {

    @Id
    private String id;
    private String title;
    private String author;
    private Date createdDate;


}
