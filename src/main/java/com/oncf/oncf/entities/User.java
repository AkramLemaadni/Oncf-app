package com.oncf.oncf.entities;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private Role role;

    @ManyToOne
    @JoinColumn(name = "supervisor_id")
    private User supervisor;

    @OneToMany(mappedBy = "supervisor")
    private List<User> supervisedUsers;

    @OneToMany(mappedBy = "assessor")
    private List<Assessment> givenAssessments;

    @OneToMany(mappedBy = "assessedUser")
    private List<Assessment> receivedAssessments;

    public enum Role {
        ENGINEER,
        TECHNICIAN,
        INTERN
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public User getSupervisor() {
        return supervisor;
    }

    public void setSupervisor(User supervisor) {
        this.supervisor = supervisor;
    }

    public List<User> getSupervisedUsers() {
        return supervisedUsers;
    }

    public void setSupervisedUsers(List<User> supervisedUsers) {
        this.supervisedUsers = supervisedUsers;
    }

    public List<Assessment> getGivenAssessments() {
        return givenAssessments;
    }

    public void setGivenAssessments(List<Assessment> givenAssessments) {
        this.givenAssessments = givenAssessments;
    }

    public List<Assessment> getReceivedAssessments() {
        return receivedAssessments;
    }

    public void setReceivedAssessments(List<Assessment> receivedAssessments) {
        this.receivedAssessments = receivedAssessments;
    }
} 