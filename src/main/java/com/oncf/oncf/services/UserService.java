package com.oncf.oncf.services;

import com.oncf.oncf.entities.User;
import com.oncf.oncf.entities.User.Role;
import com.oncf.oncf.repositories.UserRepository;
import com.oncf.oncf.security.PasswordValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User saveUser(User user) {
        // Validate and encode password if it's new or changed
        if (user.getPassword() != null) {
            PasswordValidator.validateOrThrow(user.getPassword());
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    // Role-based methods
    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRole(role);
    }

    public List<User> getEngineers() {
        return getUsersByRole(Role.ENGINEER);
    }

    public List<User> getTechnicians() {
        return getUsersByRole(Role.TECHNICIAN);
    }

    public List<User> getInterns() {
        return getUsersByRole(Role.INTERN);
    }

    // Supervisor management methods
    public List<User> getUsersBySupervisor(User supervisor) {
        return userRepository.findBySupervisor(supervisor);
    }

    public List<User> getUsersByRoleAndSupervisor(Role role, User supervisor) {
        return userRepository.findByRoleAndSupervisor(role, supervisor);
    }

    public User assignSupervisor(Long userId, Long supervisorId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        User supervisor = userRepository.findById(supervisorId)
            .orElseThrow(() -> new RuntimeException("Supervisor not found"));

        // Validate supervisor role based on user role
        validateSupervisorAssignment(user, supervisor);

        user.setSupervisor(supervisor);
        return userRepository.save(user);
    }

    private void validateSupervisorAssignment(User user, User supervisor) {
        if (user.getRole() == Role.INTERN && supervisor.getRole() != Role.TECHNICIAN) {
            throw new RuntimeException("Interns must be supervised by Technicians");
        }
        if (user.getRole() == Role.TECHNICIAN && supervisor.getRole() != Role.ENGINEER) {
            throw new RuntimeException("Technicians must be supervised by Engineers");
        }
        if (user.getRole() == Role.ENGINEER) {
            throw new RuntimeException("Engineers cannot have supervisors");
        }
    }
} 