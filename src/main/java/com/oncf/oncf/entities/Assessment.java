package com.oncf.oncf.entities;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Assessment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;
    private int score;
    private String comments;

    @ManyToOne
    @JoinColumn(name = "assessor_id")
    private User assessor;

    @ManyToOne
    @JoinColumn(name = "assessed_user_id")
    private User assessedUser;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
    
    public User getAssessor() { return assessor; }
    public void setAssessor(User assessor) { this.assessor = assessor; }
    
    public User getAssessedUser() { return assessedUser; }
    public void setAssessedUser(User assessedUser) { this.assessedUser = assessedUser; }
} 