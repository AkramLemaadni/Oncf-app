package com.oncf.oncf.controllers;

import com.oncf.oncf.entities.Assessment;
import com.oncf.oncf.entities.User;
import com.oncf.oncf.services.AssessmentService;
import com.oncf.oncf.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/engineer")
public class AssessmentController {
    
    @Autowired
    private AssessmentService assessmentService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/assess-technician/{id}")
    @PreAuthorize("hasRole('ENGINEER')")
    public String showAssessmentForm(@PathVariable("id") Long technicianId, Model model) {
        // Get the authenticated engineer
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User engineer = userService.getUserByEmail(auth.getName())
            .orElseThrow(() -> new RuntimeException("Engineer not found"));
        
        // Get the technician to be assessed
        User technician = userService.getUserById(technicianId)
            .orElseThrow(() -> new RuntimeException("Technician not found"));
        
        // Verify this technician is supervised by this engineer
        if (technician.getSupervisor() == null || !technician.getSupervisor().getId().equals(engineer.getId())) {
            throw new RuntimeException("Access denied: This technician is not under your supervision");
        }
        
        model.addAttribute("technician", technician);
        model.addAttribute("assessment", new Assessment());
        model.addAttribute("fullName", engineer.getFirstName() + " " + engineer.getLastName());
        
        return "engineer_assessment_form";
    }
    
    @PostMapping("/submit-assessment")
    @PreAuthorize("hasRole('ENGINEER')")
    @ResponseBody
    public ResponseEntity<?> submitAssessment(@RequestBody Map<String, Object> payload) {
        try {
            // Get the authenticated engineer
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User engineer = userService.getUserByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Engineer not found"));
            
            // Get the technician being assessed
            Long technicianId = Long.parseLong(payload.get("technicianId").toString());
            User technician = userService.getUserById(technicianId)
                .orElseThrow(() -> new RuntimeException("Technician not found"));
            
            // Verify this technician is supervised by this engineer
            if (technician.getSupervisor() == null || !technician.getSupervisor().getId().equals(engineer.getId())) {
                return ResponseEntity.badRequest().body("Access denied: This technician is not under your supervision");
            }
            
            // Create and populate the assessment
            Assessment assessment = new Assessment();
            assessment.setAssessor(engineer);
            assessment.setAssessedUser(technician);
            assessment.setDate(LocalDate.now());
            assessment.setScore((Integer) payload.get("score"));
            assessment.setComments((String) payload.get("comments"));
            
            // Save the assessment
            assessmentService.saveAssessment(assessment);
            
            return ResponseEntity.ok().body("Assessment submitted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error submitting assessment: " + e.getMessage());
        }
    }
    
    // REST API endpoints for assessments
    @GetMapping("/api/assessments")
    @PreAuthorize("hasRole('ENGINEER')")
    @ResponseBody
    public List<Assessment> getAllAssessments() {
        // Get the authenticated engineer
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User engineer = userService.getUserByEmail(auth.getName())
            .orElseThrow(() -> new RuntimeException("Engineer not found"));
        
        return assessmentService.getAssessmentsByAssessor(engineer);
    }
    
    @GetMapping("/api/assessments/{id}")
    @PreAuthorize("hasRole('ENGINEER')")
    @ResponseBody
    public ResponseEntity<Assessment> getAssessmentById(@PathVariable Long id) {
        // Get the authenticated engineer
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User engineer = userService.getUserByEmail(auth.getName())
            .orElseThrow(() -> new RuntimeException("Engineer not found"));
        
        Assessment assessment = assessmentService.getAssessmentById(id)
            .orElseThrow(() -> new RuntimeException("Assessment not found"));
        
        // Verify this assessment was created by this engineer
        if (!assessment.getAssessor().getId().equals(engineer.getId())) {
            throw new RuntimeException("Access denied: This assessment was not created by you");
        }
        
        return ResponseEntity.ok(assessment);
    }
} 