package com.oncf.oncf.repositories;

import com.oncf.oncf.entities.Assessment;
import com.oncf.oncf.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssessmentRepository extends JpaRepository<Assessment, Long> {
    List<Assessment> findByAssessor(User assessor);
    List<Assessment> findByAssessedUser(User assessedUser);
    List<Assessment> findByAssessorAndAssessedUser(User assessor, User assessedUser);
} 