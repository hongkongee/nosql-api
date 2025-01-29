package com.example.mongodb.myapp.service;

import com.example.mongodb.myapp.model.Block;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class BlockService {

    @Autowired
    private MongoTemplate mongoTemplate;


    public void saveBlock(Block block) {
        mongoTemplate.save(block);
    }
}
