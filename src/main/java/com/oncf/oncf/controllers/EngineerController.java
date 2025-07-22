package com.oncf.oncf.controllers;

import com.oncf.oncf.entities.User;
import com.oncf.oncf.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/engineers")
@PreAuthorize("hasRole('ENGINEER')")  // All endpoints require ENGINEER role
public class EngineerController {
    @Autowired
    private UserService userService;

    @GetMapping
    public List<User> getAllEngineers() {
        return userService.getUsersByRole(User.Role.ENGINEER);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getEngineerById(@PathVariable Long id) {
        Optional<User> engineer = userService.getUserById(id);
        if (engineer.isPresent() && engineer.get().getRole() == User.Role.ENGINEER) {
            return ResponseEntity.ok(engineer.get());
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEngineer(@PathVariable Long id, @RequestBody User user) {
        // Get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User currentUser = userService.getUserByEmail(email)
            .orElseThrow(() -> new RuntimeException("Current user not found"));

        // Only allow engineers to update their own profile
        if (!currentUser.getId().equals(id)) {
            return ResponseEntity.badRequest().body("Engineers can only update their own profile");
        }

        Optional<User> existingEngineer = userService.getUserById(id);
        if (!existingEngineer.isPresent() || existingEngineer.get().getRole() != User.Role.ENGINEER) {
            return ResponseEntity.notFound().build();
        }

        // Ensure the role cannot be changed
        user.setId(id);
        user.setRole(User.Role.ENGINEER);
        
        return ResponseEntity.ok(userService.saveUser(user));
    }

    @GetMapping("/current")
    public ResponseEntity<User> getCurrentEngineer() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Optional<User> engineer = userService.getUserByEmail(email);
        
        if (engineer.isPresent() && engineer.get().getRole() == User.Role.ENGINEER) {
            return ResponseEntity.ok(engineer.get());
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/my-team")
    public ResponseEntity<?> getMyTeam() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User engineer = userService.getUserByEmail(email)
            .orElseThrow(() -> new RuntimeException("Engineer not found"));

        if (engineer.getRole() != User.Role.ENGINEER) {
            return ResponseEntity.badRequest().body("Only engineers can access this endpoint");
        }

        // Get all technicians supervised by this engineer
        List<User> technicians = userService.getUsersByRoleAndSupervisor(User.Role.TECHNICIAN, engineer);
        
        return ResponseEntity.ok(technicians);
    }
} 