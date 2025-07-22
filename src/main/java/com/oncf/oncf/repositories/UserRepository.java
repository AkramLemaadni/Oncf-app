package com.oncf.oncf.repositories;

import com.oncf.oncf.entities.User;
import com.oncf.oncf.entities.User.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByRole(Role role);
    List<User> findBySupervisor(User supervisor);
    List<User> findByRoleAndSupervisor(Role role, User supervisor);
} 