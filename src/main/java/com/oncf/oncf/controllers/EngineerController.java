package com.oncf.oncf.controllers;

import com.oncf.oncf.entities.User;
import com.oncf.oncf.entities.Assessment;
import com.oncf.oncf.services.UserService;
import com.oncf.oncf.services.AssessmentService;
import com.oncf.oncf.services.StatisticsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/engineer")
@PreAuthorize("hasRole('ENGINEER')")
public class EngineerController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private AssessmentService assessmentService;
    
    @Autowired
    private StatisticsService statisticsService;
    
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
    
    // === STATISTICS API ENDPOINTS ===
    
    @PostMapping("/api/upload-xlsx")
    @ResponseBody
    public ResponseEntity<?> uploadXlsxFile(@RequestParam("file") MultipartFile file) {
        try {
            // Get the authenticated engineer
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User engineer = userService.getUserByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Engineer not found"));
            
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Please select a file to upload");
            }
            
            if (!file.getOriginalFilename().toLowerCase().endsWith(".xlsx")) {
                return ResponseEntity.badRequest().body("Only .xlsx files are supported");
            }
            
            // Process the file
            String sessionId = statisticsService.processXlsxFile(file, engineer.getEmail());
            
            return ResponseEntity.ok().body(java.util.Map.of(
                "success", true,
                "message", "File uploaded successfully",
                "sessionId", sessionId
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of(
                "success", false,
                "message", "Error uploading file: " + e.getMessage()
            ));
        }
    }
    
    @GetMapping("/api/files")
    @ResponseBody
    public ResponseEntity<?> getUserFiles() {
        try {
            // Get the authenticated engineer
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User engineer = userService.getUserByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Engineer not found"));
            
            List<java.util.Map<String, Object>> files = statisticsService.getUserFiles(engineer.getEmail());
            
            return ResponseEntity.ok(files);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error retrieving files: " + e.getMessage());
        }
    }
    
    @GetMapping("/api/file/{fileId}")
    @ResponseBody
    public ResponseEntity<?> getFileData(@PathVariable String fileId) {
        try {
            java.util.Map<String, Object> fileData = statisticsService.getFileData(fileId);
            
            if (fileData == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(fileData);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error retrieving file data: " + e.getMessage());
        }
    }
    
    @GetMapping("/api/file/{fileId}/incidents")
    @ResponseBody
    public ResponseEntity<?> getIncidentAnalysis(@PathVariable String fileId) {
        try {
            java.util.Map<String, Object> incidentData = statisticsService.getIncidentAnalysis(fileId);
            
            if (incidentData == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(incidentData);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error analyzing incidents: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/api/file/{fileId}")
    @ResponseBody
    public ResponseEntity<?> deleteFile(@PathVariable String fileId) {
        try {
            statisticsService.deleteFile(fileId);
            
            return ResponseEntity.ok().body(java.util.Map.of(
                "success", true,
                "message", "Session deleted successfully"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting session: " + e.getMessage());
        }
    }
} 