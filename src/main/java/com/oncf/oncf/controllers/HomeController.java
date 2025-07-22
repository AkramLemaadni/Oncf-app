package com.oncf.oncf.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
    
    @GetMapping("/test-login")
    public String testLogin() {
        return "test_login";
    }
    
    @GetMapping("/api-test")
    public String apiTest() {
        return "api_test";
    }
} 