package com.oncf.oncf.controllers;

import com.oncf.oncf.entities.Assessment;
import com.oncf.oncf.services.AssessmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/assessments")
public class AssessmentController {
    @Autowired
    private AssessmentService assessmentService;

    @GetMapping
    public List<Assessment> getAllAssessments() {
        return assessmentService.getAllAssessments();
    }

    @GetMapping("/{id}")
    public Optional<Assessment> getAssessmentById(@PathVariable Long id) {
        return assessmentService.getAssessmentById(id);
    }

    @PostMapping
    public Assessment createAssessment(@RequestBody Assessment assessment) {
        return assessmentService.saveAssessment(assessment);
    }

    @PutMapping("/{id}")
    public Assessment updateAssessment(@PathVariable Long id, @RequestBody Assessment assessment) {
        assessment.setId(id);
        return assessmentService.saveAssessment(assessment);
    }

    @DeleteMapping("/{id}")
    public void deleteAssessment(@PathVariable Long id) {
        assessmentService.deleteAssessment(id);
    }
} 