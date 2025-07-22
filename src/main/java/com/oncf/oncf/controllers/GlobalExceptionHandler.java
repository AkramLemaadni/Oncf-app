package com.oncf.oncf.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<String> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        logger.error("Method not supported exception: {}", ex.getMessage());
        logger.error("Request method: {}", ex.getMethod());
        logger.error("Supported methods: {}", String.join(", ", ex.getSupportedMethods()));
        return new ResponseEntity<>("Method not supported: " + ex.getMessage(), HttpStatus.METHOD_NOT_ALLOWED);
    }
} 