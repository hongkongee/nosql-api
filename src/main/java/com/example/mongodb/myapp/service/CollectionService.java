package com.example.mongodb.myapp.service;

import com.mongodb.client.MongoDatabase;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class CollectionService {

    @Autowired
    private MongoTemplate mongoTemplate;


    private final CollectionRepository collectionRepository;


    public String createCollection(String collectionName) {
        mongoTemplate.createCollection(collectionName);
        return "Collection " + collectionName + " created.";
    }

    public ArrayList<String> getAllCollections() {
        MongoDatabase database = mongoTemplate.getDb();
        return database.listCollectionNames().into(new ArrayList<>());
    }

}
