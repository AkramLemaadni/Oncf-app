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
    public String submitAssessment(@ModelAttribute("assessment") Assessment assessment, 
                                  @RequestParam("technicianId") Long technicianId,
                                  @RequestParam(value = "jwtToken", required = false) String jwtToken,
                                  RedirectAttributes redirectAttributes) {
        // Get the authenticated engineer
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User engineer = userService.getUserByEmail(auth.getName())
            .orElseThrow(() -> new RuntimeException("Engineer not found"));
        
        // Get the technician being assessed
        User technician = userService.getUserById(technicianId)
            .orElseThrow(() -> new RuntimeException("Technician not found"));
        
        // Set assessment properties
        assessment.setAssessor(engineer);
        assessment.setAssessedUser(technician);
        assessment.setDate(LocalDate.now());
        
        // Save the assessment
        try {
            assessmentService.saveAssessment(assessment);
            redirectAttributes.addFlashAttribute("successMessage", "Assessment submitted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error submitting assessment: " + e.getMessage());
        }
        
        return "redirect:/engineer/technicians";
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