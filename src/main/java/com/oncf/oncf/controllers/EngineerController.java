package com.oncf.oncf.controllers;

import com.oncf.oncf.entities.User;
import com.oncf.oncf.entities.Assessment;
import com.oncf.oncf.services.UserService;
import com.oncf.oncf.services.AssessmentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/engineer")
@PreAuthorize("hasRole('ENGINEER')")
public class EngineerController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private AssessmentService assessmentService;
    
    @GetMapping("/technicians")
    public String listTechnicians(Model model) {
        // Get the authenticated engineer
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User engineer = userService.getUserByEmail(auth.getName())
            .orElseThrow(() -> new RuntimeException("Engineer not found"));
        
        // Get technicians supervised by this engineer
        List<User> technicians = userService.getUsersByRoleAndSupervisor(User.Role.TECHNICIAN, engineer);
        
        model.addAttribute("technicians", technicians);
        model.addAttribute("fullName", engineer.getFirstName() + " " + engineer.getLastName());
        
        return "engineer_technicians";
    }
    
    @GetMapping("/technician/{id}")
    public String viewTechnician(@PathVariable("id") Long technicianId, Model model) {
        // Get the authenticated engineer
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User engineer = userService.getUserByEmail(auth.getName())
            .orElseThrow(() -> new RuntimeException("Engineer not found"));
            
        // Get the technician
        User technician = userService.getUserById(technicianId)
            .orElseThrow(() -> new RuntimeException("Technician not found"));
            
        // Verify this technician is supervised by this engineer
        if (technician.getSupervisor() == null || !technician.getSupervisor().getId().equals(engineer.getId())) {
            throw new RuntimeException("Access denied: This technician is not under your supervision");
        }
        
        // Get previous assessments for this technician
        List<Assessment> assessments = assessmentService.getAssessmentsByAssessedUser(technician);
        
        model.addAttribute("technician", technician);
        model.addAttribute("assessments", assessments);
        model.addAttribute("fullName", engineer.getFirstName() + " " + engineer.getLastName());
        
        return "engineer_technician_detail";
    }
    
    @GetMapping("/stats")
    public String showStats(Model model) {
        // Get the authenticated engineer
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User engineer = userService.getUserByEmail(auth.getName())
            .orElseThrow(() -> new RuntimeException("Engineer not found"));
            
        model.addAttribute("fullName", engineer.getFirstName() + " " + engineer.getLastName());
        
        return "engineer_stats";
    }
    
    @GetMapping("/interns")
    public String listInterns(Model model) {
        // Get the authenticated engineer
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User engineer = userService.getUserByEmail(auth.getName())
            .orElseThrow(() -> new RuntimeException("Engineer not found"));
            
        model.addAttribute("fullName", engineer.getFirstName() + " " + engineer.getLastName());
        
        return "engineer_interns";
    }
} 