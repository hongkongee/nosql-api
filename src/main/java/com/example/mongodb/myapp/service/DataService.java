package com.example.mongodb.myapp.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;

import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
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
        // 조건 쿼리문 생성
        Query query = getQuery(criteriaJson);

        // 프로젝션 설정
        if (projectionJson != null) {
            Iterator<String> projectionFieldNames = projectionJson.fieldNames();
            while (projectionFieldNames.hasNext()) {
                String fieldName = projectionFieldNames.next();
                query.fields().exclude("_id").include(fieldName);
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

    public String updateDocuments(JsonNode filterJson, JsonNode updateJson, String collectionName) throws Exception {

        try {

        // 수정 조건 생성
        Query query = getQuery(filterJson);

        // 수정 내용 생성
        Update update = new Update();
        // update 부분을 동적으로 처리
        processUpdateFields(update, updateJson);

        // updateMany 실행
        mongoTemplate.updateMulti(query, update, "collection_name");
        return "Successfully updated data into collection " + collectionName;
        } catch (Exception e) {
            e.printStackTrace(); // 예외 처리
            throw new Exception();
        }
    }

    private void processUpdateFields(Update update, JsonNode updateJson) {
        Iterator<String> fieldNames = updateJson.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            JsonNode fieldValue = updateJson.get(fieldName);
            // 필드 타입에 따라 처리
            if (fieldValue.isInt()) {
                update.set(fieldName, fieldValue.asInt());
            } else if (fieldValue.isDouble()) {
                update.set(fieldName, fieldValue.asDouble());
            } else if (fieldValue.isBoolean()) {
                update.set(fieldName, fieldValue.asBoolean());
            } else if (fieldValue.isTextual()) {
                update.set(fieldName, fieldValue.asText());
            } else if (fieldValue.isArray()) {
                // 배열 처리 (일단 String으로 처리)
                List<String> list = new ArrayList<>();
                for (JsonNode arrayItem : fieldValue) {
                    list.add(arrayItem.asText());
                }
                update.set(fieldName, list);
            } else {
                // 필요한 경우 다른 타입 처리 추가
                update.set(fieldName, fieldValue.asText());
            }
        }
    }

    @NotNull
    private Query getQuery(JsonNode criteriaJson) {
        Query query = new Query();

        // 검색 조건 추가
        if (criteriaJson != null) {
            Iterator<String> fieldNames = criteriaJson.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode valueNode = criteriaJson.get(fieldName);
                // 조건에 따라 Criteria 추가
                if (valueNode.isArray()) {
                    // and 또는 or 문
                    // OR 조건 처리
                    if (criteriaJson.has("$or")) {
                        log.info("or begin");
                        List<Criteria> orCriteriaList = new ArrayList<>();
                        // criteriaJson.get("$or")은 array
                        for (JsonNode orNode : criteriaJson.get("$or")) {
                            orCriteriaList.add(parseCriteria(orNode));
                        }
                        query.addCriteria(new Criteria().orOperator(orCriteriaList.toArray(new Criteria[0])));
                    }

                    // AND 조건 처리
                    if (criteriaJson.has("$and")) {
                        log.info("and begin");
                        List<Criteria> andCriteriaList = new ArrayList<>();
                        for (JsonNode andNode : criteriaJson.get("$and")) {
                            andCriteriaList.add(parseCriteria(andNode));
                        }
                        query.addCriteria(new Criteria().andOperator(andCriteriaList.toArray(new Criteria[0])));
                    }

                } else if (valueNode.isObject()) {
                    // valueNode = { "$in": ["Changwon"] }
                    // $ 옵션 처리
                    addCriteriaFromObject(query, fieldName, valueNode);
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
        return query;
    }

    private void addCriteriaFromObject(Query query, String fieldName, JsonNode valueNode) {
        // valueNode = { "$in": ["Changwon"] }
        // 조건에 따라서 Criteria를 추가
        if (valueNode.has("$gte")) {
            query.addCriteria(Criteria.where(fieldName).gte(valueNode.get("$gte").asInt()));
        }
        if (valueNode.has("$lte")) {
            query.addCriteria(Criteria.where(fieldName).lte(valueNode.get("$lte").asInt()));
        }
        if (valueNode.has("$gt")) {
            query.addCriteria(Criteria.where(fieldName).gt(valueNode.get("$gt").asInt()));
        }
        if (valueNode.has("$lt")) {
            query.addCriteria(Criteria.where(fieldName).lt(valueNode.get("$lt").asInt()));
        }
        if (valueNode.has("$eq")) {
            query.addCriteria(Criteria.where(fieldName).is(valueNode.get("$eq").asInt()));
        }
        if (valueNode.has("$ne")) {
            query.addCriteria(Criteria.where(fieldName).ne(valueNode.get("$ne").asInt()));
        }
        if (valueNode.has("$nin")) {
            List<String> ninList = new ArrayList<>();
            for (JsonNode ninNode : valueNode.get("$nin")) {
                ninList.add(ninNode.asText());
            }
            query.addCriteria(Criteria.where(fieldName).nin(ninList));
        }
        if (valueNode.has("$in")) {
            List<String> inList = new ArrayList<>();
            for (JsonNode inNode : valueNode.get("$in")) {
                inList.add(inNode.asText());
            }
            query.addCriteria(Criteria.where(fieldName).in(inList));
        }
    }

    // 조건 처리 로직 (json object를 넣으면 조건문 반환)
    private Criteria parseCriteria(JsonNode criteriaNode) {
        Criteria criteria = new Criteria();

        // criteriaNode = { "age": { "$gte": 25, $lte": 30  } }
        criteriaNode.fieldNames().forEachRemaining(fieldName -> {
            // conditionNode = { "$gte": 25, $lte": 30  } -> 여러개의 $ 필드가 있는 경우 고려 필요
            JsonNode conditionNode = criteriaNode.get(fieldName);
            if (conditionNode.isObject()) {
                // 각 연산자에 따라 처리
                if (conditionNode.has("$gte")) {
                    criteria.and(fieldName).gte(conditionNode.get("$gte").asInt());
                } else if (conditionNode.has("$lte")) {
                    criteria.and(fieldName).lte(conditionNode.get("$lte").asInt());
                } else if (conditionNode.has("$gt")) {
                    criteria.and(fieldName).gt(conditionNode.get("$gt").asInt());
                } else if (conditionNode.has("$lt")) {
                    criteria.and(fieldName).lt(conditionNode.get("$lt").asInt());
                } else if (conditionNode.has("$eq")) {
                    criteria.and(fieldName).is(conditionNode.get("$eq").asInt());
                } else if (conditionNode.has("$ne")) {
                    criteria.and(fieldName).ne(conditionNode.get("$ne").asInt());
                } else if (conditionNode.has("$nin")) {
                    List<String> ninList = new ArrayList<>();
                    conditionNode.get("$nin").forEach(ninNode -> ninList.add(ninNode.asText()));
                    criteria.and(fieldName).nin(ninList);
                } else if (conditionNode.has("$in")) {
                    List<String> inList = new ArrayList<>();
                    conditionNode.get("$in").forEach(inNode -> inList.add(inNode.asText()));
                    criteria.and(fieldName).in(inList);
                }
            } else {
                if (criteriaNode.isInt()) {
                    criteria.and(fieldName).is(criteriaNode.asInt());
                } else if (criteriaNode.isTextual()) {
                    criteria.and(fieldName).is(criteriaNode.asText());
                }
            }

            log.info(String.valueOf(criteria));
        });
        return criteria;
    }



}
