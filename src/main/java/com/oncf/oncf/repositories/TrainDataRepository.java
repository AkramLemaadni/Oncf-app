package com.oncf.oncf.repositories;

import com.oncf.oncf.entities.TrainData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface TrainDataRepository extends JpaRepository<TrainData, String> {
    
    // Find all files uploaded by a specific engineer
    List<TrainData> findByUploadedByOrderByUploadDateDesc(String uploadedBy);
    
    // Check if a file with the same ID already exists (same day upload)
    boolean existsById(String id);
} 