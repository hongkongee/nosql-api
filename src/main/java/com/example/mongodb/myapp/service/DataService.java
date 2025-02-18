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

    public String insertDocuments(String collectionName, String jsonArrayString) throws Exception {
        try {
            MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

            // JSON 문자열을 List<Document>로 변환 -> mongoTemplate 방식
            List<Document> documents = objectMapper.readValue(jsonArrayString, new TypeReference<List<Document>>() {});
            mongoTemplate.insert(documents, collectionName);
            return "Successfully inserted data into collection " + collectionName;

        } catch (Exception e) {
            e.printStackTrace(); // 예외 처리
            throw new Exception("Error while inserting documents", e);
        }
    }

    public List<Document> findDocuments(JsonNode criteriaJson, JsonNode projectionJson, JsonNode sortJson, String collectionName) throws Exception {

        try {
            // 조건 쿼리문 생성
            Query query = getQuery(criteriaJson);
            // Query query = new Query();

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

        } catch (Exception e) {
            e.printStackTrace(); // 예외 처리
            throw new Exception("Error while finding documents", e);
        }
    }

    public String updateDocuments(JsonNode filterJson, JsonNode updateJson, Boolean isUpsert, String collectionName) throws Exception {

        try {

            // 수정 조건 생성 (WHERE 절)
            Query query = getQuery(filterJson);

            if (query == null) {
            throw new IllegalArgumentException("Invalid filter conditions");
            }

            // 수정 내용 생성 (SET 절)
            // update 부분을 동적으로 처리
            Update update = processUpdateFields(updateJson);


            // Upsert 처리
            if (isUpsert) {
                mongoTemplate.upsert(query, update, collectionName);
            } else {
                mongoTemplate.updateMulti(query, update, collectionName);
            }


            return "Successfully updated data into collection " + collectionName;
        } catch (Exception e) {
            e.printStackTrace(); // 예외 처리
            throw new Exception("Error while updating documents", e);
        }
    }

    public String deleteDocuments(JsonNode filterJson, String collectionName) throws Exception {
        try {

            Query query = new Query();

            if (filterJson != null && filterJson.size() > 0) {
                // 조건이 있을 경우 필터 처리 (조건을 담은 Query 객체 생성)
                query = getQuery(filterJson);
            }

            if (query == null) {
                throw new IllegalArgumentException("Invalid filter conditions");
            }


            mongoTemplate.remove(query, collectionName);



            return "Successfully deleted data from collection " + collectionName;
        } catch (Exception e) {
            e.printStackTrace(); // 예외 처리
            throw new Exception("Error while removing documents", e);
        }
    }

    /**
     *
     * @param updateJson Json 형식의 SET절 데이터 (필드명-값 pair)
     * @return Update 타입
     */
    private Update processUpdateFields(JsonNode updateJson) {
        Update update = new Update();

        Iterator<String> fieldNames = updateJson.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            JsonNode fieldValue = updateJson.get(fieldName);

            // 필드 값에 따라 다른 업데이트 처리
            if (fieldName.equals("set")) {
                fieldValue.forEach(field -> {


                    String dataFieldName = field.fieldNames().next();
                    update.set(dataFieldName, field.get(dataFieldName).asText());
                });
            } else if (fieldName.equals("unset")) {
                fieldValue.forEach(field -> {
                    String dataFieldName = field.fieldNames().next();
                    update.unset(dataFieldName);
                });
            } else if (fieldName.equals("arrayPush")) {
                fieldValue.forEach(field -> {
                    String dataFieldName = field.fieldNames().next();
                    update.push(dataFieldName, field.get(dataFieldName).asText());
                });
            } else if (fieldName.equals("arraySetPush")) {
                fieldValue.forEach(field -> {
                    String dataFieldName = field.fieldNames().next();
                    update.addToSet(dataFieldName, field.get(dataFieldName).asText());
                });
            } else if (fieldName.equals("arrayPull")) {
                fieldValue.forEach(field -> {
                    String dataFieldName = field.fieldNames().next();
                    update.pull(dataFieldName, field.get(dataFieldName).asText());
                });
            }



            /*
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
            } else if (fieldValue.isObject()) {
                // 오브젝트 타입: 특수한 SET
                update.set(fieldName, fieldValue.asText());
            } else {
                // 필요한 경우 다른 타입 처리 추가
                update.set(fieldName, fieldValue.asText());
            }
        }

             */

        }
        return update;
    }

    /**
     *
     * @param criteriaJson Json 형식의 조건(WHERE) 데이터
     * @return query 쿼리 객체
     * @exception IllegalArgumentException 입력 파라미터 criteria가 null값인 경우 예외 처리
     */
    @NotNull
    private Query getQuery(JsonNode criteriaJson) {
        if (criteriaJson == null) {
            throw new IllegalArgumentException("criteriaJson cannot be null");
        }

        Query query = new Query();

        // 검색 조건 추가
        if (criteriaJson != null) {
            Iterator<String> fieldNames = criteriaJson.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode valueNode = criteriaJson.get(fieldName);
                // 조건에 따라 Criteria 추가
                if (valueNode.isArray()) {
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
                    // $ 옵션 처리 (특정 범위인 경우를 조회)
                    Criteria newCriteria = addCriteriaFromObject(fieldName, valueNode);
                    query.addCriteria(newCriteria);


                } else {
                    // 필드가 특정 값인 경우를 조회
                    if (valueNode.isInt()) {
                        // 숫자 타입인 경우
                        query.addCriteria(Criteria.where(fieldName).is(valueNode.asInt()));
                    } else if (valueNode.isTextual()) {
                        // 그 외 타입인 경우
                        query.addCriteria(Criteria.where(fieldName).is(valueNode.asText()));
                    }
                }
            }
        }
        return query;
    }

    /**
    * 조건과 관련한 키-값 pair를 전달하면 쿼리의 Criteria(조건문)을 생성
     *
    * @param fieldName 적용할 필드 이름
     * @param valueNode Json 형식의 조건 데이터
     * @return creteria 생성된 Criteria 객체
    */
    private Criteria addCriteriaFromObject(String fieldName, JsonNode valueNode) {
        Query query = new Query();
        Criteria creteria = new Criteria();

        // valueNode = { "$in": ["Changwon"] }
        // 조건에 따라서 Criteria를 추가
        if (valueNode.has("$gte")) {
            query.addCriteria(Criteria.where(fieldName).gte(valueNode.get("$gte").asInt()));
            creteria = creteria.and(fieldName).gte(valueNode.get("$gte").asInt());
        }
        if (valueNode.has("$lte")) {
            query.addCriteria(Criteria.where(fieldName).lte(valueNode.get("$lte").asInt()));
            creteria = creteria.and(fieldName).lte(valueNode.get("$lte").asInt());
        }
        if (valueNode.has("$gt")) {
            query.addCriteria(Criteria.where(fieldName).gt(valueNode.get("$gt").asInt()));
            creteria = creteria.and(fieldName).gt(valueNode.get("$gt").asInt());
        }
        if (valueNode.has("$lt")) {
            query.addCriteria(Criteria.where(fieldName).lt(valueNode.get("$lt").asInt()));
            creteria = creteria.and(fieldName).lt(valueNode.get("$lt").asInt());
        }
        if (valueNode.has("$eq")) {
            query.addCriteria(Criteria.where(fieldName).is(valueNode.get("$eq").asInt()));
            creteria = creteria.and(fieldName).is(valueNode.get("$eq").asInt());
        }
        if (valueNode.has("$ne")) {
            query.addCriteria(Criteria.where(fieldName).ne(valueNode.get("$ne").asInt()));
            creteria = creteria.and(fieldName).ne(valueNode.get("$ne").asInt());
        }
        if (valueNode.has("$nin")) {
            List<String> ninList = new ArrayList<>();
            for (JsonNode ninNode : valueNode.get("$nin")) {
                ninList.add(ninNode.asText());
            }
            query.addCriteria(Criteria.where(fieldName).nin(ninList));
            creteria = creteria.and(fieldName).nin(ninList);
        }
        if (valueNode.has("$in")) {
            List<String> inList = new ArrayList<>();
            for (JsonNode inNode : valueNode.get("$in")) {
                inList.add(inNode.asText());
            }
            query.addCriteria(Criteria.where(fieldName).in(inList));
            creteria = creteria.and(fieldName).in(inList);
        }
        return creteria;
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
