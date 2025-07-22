package com.oncf.oncf.services;

import com.oncf.oncf.entities.Assessment;
import com.oncf.oncf.entities.User;
import com.oncf.oncf.entities.User.Role;
import com.oncf.oncf.repositories.AssessmentRepository;
import com.oncf.oncf.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AssessmentService {
    @Autowired
    private AssessmentRepository assessmentRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Assessment> getAllAssessments() {
        return assessmentRepository.findAll();
    }

    public Optional<Assessment> getAssessmentById(Long id) {
        return assessmentRepository.findById(id);
    }

    public Assessment saveAssessment(Assessment assessment) {
        validateAssessment(assessment);
        return assessmentRepository.save(assessment);
    }

    public void deleteAssessment(Long id) {
        assessmentRepository.deleteById(id);
    }

    public List<Assessment> getAssessmentsByAssessor(User assessor) {
        return assessmentRepository.findByAssessor(assessor);
    }

    public List<Assessment> getAssessmentsByAssessedUser(User assessedUser) {
        return assessmentRepository.findByAssessedUser(assessedUser);
    }

    private void validateAssessment(Assessment assessment) {
        User assessor = assessment.getAssessor();
        User assessed = assessment.getAssessedUser();

        if (assessor == null || assessed == null) {
            throw new RuntimeException("Both assessor and assessed user must be specified");
        }

        // Engineers can assess Technicians
        if (assessor.getRole() == Role.ENGINEER && assessed.getRole() != Role.TECHNICIAN) {
            throw new RuntimeException("Engineers can only assess Technicians");
        }

        // Technicians can assess Interns
        if (assessor.getRole() == Role.TECHNICIAN && assessed.getRole() != Role.INTERN) {
            throw new RuntimeException("Technicians can only assess Interns");
        }

        // Interns cannot assess anyone
        if (assessor.getRole() == Role.INTERN) {
            throw new RuntimeException("Interns cannot perform assessments");
        }

        // Verify supervisor relationship
        if (!isValidSupervisor(assessor, assessed)) {
            throw new RuntimeException("Assessor must be the supervisor of the assessed user");
        }
    }

    private boolean isValidSupervisor(User assessor, User assessed) {
        return assessed.getSupervisor() != null && assessed.getSupervisor().getId().equals(assessor.getId());
    }
} 