package com.example.mongodb.myapp.service;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class CollectionService {

    @Autowired
    private MongoTemplate mongoTemplate;

    public CollectionService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }


    public String createCollection(String collectionName) {
        mongoTemplate.createCollection(collectionName);
        return "Collection " + collectionName + " created.";
    }

    public ArrayList<String> getAllCollections() {
        MongoDatabase database = mongoTemplate.getDb();
        return database.listCollectionNames().into(new ArrayList<>());
    }

    public String deleteCollection(String collectionName) {
        mongoTemplate.getDb().getCollection(collectionName).drop();
        return "Collection " + collectionName + " removed.";
    }

    public String truncateCollection(String collectionName) {
        mongoTemplate.remove(new Query(), collectionName);
        return "Collection " + collectionName + "'s all data are removed.";
    }

//    public void getCollectionMetadata(String collectionName) {
//        MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
//        return collection.findOneAndUpdate(new Document("collStats", collectionName));
//    }
}
