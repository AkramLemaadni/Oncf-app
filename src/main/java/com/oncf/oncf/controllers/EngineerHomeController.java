package com.oncf.oncf.controllers;

import com.oncf.oncf.entities.User;
import com.oncf.oncf.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

@Controller
public class EngineerHomeController {

    private static final Logger logger = LoggerFactory.getLogger(EngineerHomeController.class);

    @Autowired
    private UserService userService;

    @GetMapping("/engineer/home")
    @PreAuthorize("hasRole('ENGINEER')")
    public String engineerHome(Model model) {
        // Get the authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        
        logger.debug("Loading engineer home page for user: {}", userEmail);
        
        // Find the user in the database
        User user = userService.getUserByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));
        
        // Verify user is an engineer
        if (user.getRole() != User.Role.ENGINEER) {
            logger.error("Non-engineer user attempting to access engineer home: {}", userEmail);
            throw new RuntimeException("Access denied: User is not an engineer");
        }

        String fullName = user.getFirstName() + " " + user.getLastName();
        logger.debug("Setting welcome message for: {}", fullName);
        
        // Add user information to model
            model.addAttribute("firstName", user.getFirstName());
            model.addAttribute("lastName", user.getLastName());
        model.addAttribute("fullName", fullName);
        
        return "engineer_home";
    }
} 