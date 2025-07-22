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
@RequestMapping("/api/technicians")
public class TechnicianController {
    @Autowired
    private UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ENGINEER')")
    public List<User> getAllTechnicians() {
        return userService.getUsersByRole(User.Role.TECHNICIAN);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ENGINEER')")
    public ResponseEntity<User> getTechnicianById(@PathVariable Long id) {
        Optional<User> technician = userService.getUserById(id);
        if (technician.isPresent() && technician.get().getRole() == User.Role.TECHNICIAN) {
            return ResponseEntity.ok(technician.get());
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ENGINEER')")
    public ResponseEntity<?> updateTechnician(@PathVariable Long id, @RequestBody User user) {
        Optional<User> existingTechnician = userService.getUserById(id);
        if (!existingTechnician.isPresent() || existingTechnician.get().getRole() != User.Role.TECHNICIAN) {
            return ResponseEntity.notFound().build();
        }

        // Ensure the role cannot be changed
        user.setId(id);
        user.setRole(User.Role.TECHNICIAN);
        
        // Validate supervisor is an engineer
        if (user.getSupervisor() != null && user.getSupervisor().getRole() != User.Role.ENGINEER) {
            return ResponseEntity.badRequest().body("Supervisor must be an Engineer");
        }

        return ResponseEntity.ok(userService.saveUser(user));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ENGINEER')")
    public ResponseEntity<?> deleteTechnician(@PathVariable Long id) {
        Optional<User> technician = userService.getUserById(id);
        if (!technician.isPresent() || technician.get().getRole() != User.Role.TECHNICIAN) {
            return ResponseEntity.notFound().build();
        }
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/my-technicians")
    @PreAuthorize("hasRole('ENGINEER')")
    public List<User> getMyTechnicians() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User engineer = userService.getUserByEmail(email)
            .orElseThrow(() -> new RuntimeException("Engineer not found"));
        
        if (engineer.getRole() != User.Role.ENGINEER) {
            throw new RuntimeException("Only engineers can access this endpoint");
        }

        return userService.getUsersByRoleAndSupervisor(User.Role.TECHNICIAN, engineer);
    }
} 