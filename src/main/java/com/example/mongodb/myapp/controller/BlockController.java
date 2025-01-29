package com.example.mongodb.myapp.controller;

import com.example.mongodb.myapp.model.Block;
import com.example.mongodb.myapp.service.BlockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/blocks")
public class BlockController {

    @Autowired
    private BlockService blockService;

    @PostMapping
    public ResponseEntity<String> createBlock(@RequestBody Block block) {
        blockService.saveBlock(block);
        return ResponseEntity.ok("Block saved successfully.");
    }

}
