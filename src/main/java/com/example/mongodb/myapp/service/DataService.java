package com.example.mongodb.myapp.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;

import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
@Slf4j
public class DataService {
    @Autowired
    private final MongoTemplate mongoTemplate;
    private final ObjectMapper objectMapper;

    public DataService(MongoTemplate mongoTemplate, ObjectMapper objectMapper) {
        this.mongoTemplate = mongoTemplate;
        this.objectMapper = objectMapper;
    }

    public String insertDocumentsFromJson(String collectionName, String jsonArrayString) throws Exception {
        try {
            MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

            // JSON 문자열을 List<Document>로 변환 -> mongoTemplate 방식
            List<Document> documents = objectMapper.readValue(jsonArrayString, new TypeReference<List<Document>>() {});
            mongoTemplate.insert(documents, collectionName);
            return "Successfully inserted data into collection " + collectionName;

        } catch (Exception e) {
            e.printStackTrace(); // 예외 처리
            throw new Exception();
        }
    }

    public List<Document> findDocuments(JsonNode criteriaJson, JsonNode projectionJson, JsonNode sortJson, String collectionName) {
        Query query = new Query();

        // 검색 조건 추가
        if (criteriaJson != null) {
            Iterator<String> fieldNames = criteriaJson.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode valueNode = criteriaJson.get(fieldName);
                // 조건에 따라 Criteria 추가
                if (valueNode.isObject()) {
                    if (valueNode.has("$gte")) {
                        query.addCriteria(Criteria.where(fieldName).gte(valueNode.get("$gte").asInt()));
                    } else if (valueNode.has("$lte")) {
                        query.addCriteria(Criteria.where(fieldName).lte(valueNode.get("$lte").asInt()));
                    } else if (valueNode.has("$gt")) {
                        query.addCriteria(Criteria.where(fieldName).gt(valueNode.get("$gt").asInt()));
                    } else if (valueNode.has("$lt")) {
                        query.addCriteria(Criteria.where(fieldName).lt(valueNode.get("$lt").asInt()));
                    } else if (valueNode.has("$eq")) {
                        query.addCriteria(Criteria.where(fieldName).is(valueNode.get("$eq").asInt()));
                    } else if (valueNode.has("$ne")) {
                        query.addCriteria(Criteria.where(fieldName).ne(valueNode.get("$ne").asInt()));
                    } else if (valueNode.has("$nin")) {
                        JsonNode jsonArray = valueNode.get("$nin");

                        // JsonNode를 List<String>으로 변환
                        List<String> ninList = new ArrayList<>();
                        for (JsonNode ninNode : jsonArray) {
                            ninList.add(ninNode.asText());
                        }
                        query.addCriteria(Criteria.where(fieldName).nin(ninList));
                    } else if (valueNode.has("$in")) {
                        JsonNode jsonArray = valueNode.get("$in");

                        // JsonNode를 List<String>으로 변환
                        List<String> inList = new ArrayList<>();
                        for (JsonNode inNode : jsonArray) {
                            inList.add(inNode.asText());
                        }
                        query.addCriteria(Criteria.where(fieldName).in(inList));
                    }
                } else {
                    if (valueNode.isInt()) {
                        // 숫자 타입인 경우
                        query.addCriteria(Criteria.where(fieldName).is(valueNode.asInt()));
                    } else if (valueNode.isTextual()) {
                        // 문자열 타입 (또는 다른 타입)인 경우
                        query.addCriteria(Criteria.where(fieldName).is(valueNode.asText()));
                    }
                }
            }
        }

        // 프로젝션 설정
        if (projectionJson != null) {
            Iterator<String> projectionFieldNames = projectionJson.fieldNames();
            while (projectionFieldNames.hasNext()) {
                String fieldName = projectionFieldNames.next();
                query.fields().include(fieldName);
            }
        }

        // 정렬 설정
        /*
        if (sortJson != null) {
            Iterator<String> sortFieldNames = sortJson.fieldNames();
            while (sortFieldNames.hasNext()) {
                String fieldName = sortFieldNames.next();
                int order = sortJson.get(fieldName).asInt();
                query.with(Sort.by(order == 1 ? Sort.Order.asc(fieldName) : Sort.Order.desc(fieldName)));
            }
        }

         */

        // 도큐먼트 검색
        List<Document> documentList = mongoTemplate.find(query, Document.class, collectionName);
        return  documentList; // 적절한 컬렉션 이름으로 변경
    }
}
