package com.oncf.oncf.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private static final Logger logger = LoggerFactory.getLogger(TestController.class);

    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        logger.info("Test GET endpoint called");
        return ResponseEntity.ok("Hello from GET endpoint!");
    }

    @PostMapping("/hello")
    public ResponseEntity<String> helloPost(@RequestBody(required = false) String body) {
        logger.info("Test POST endpoint called with body: {}", body);
        return ResponseEntity.ok("Hello from POST endpoint!");
    }
} 