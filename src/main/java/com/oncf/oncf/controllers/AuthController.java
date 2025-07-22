package com.oncf.oncf.controllers;

import com.oncf.oncf.payload.request.LoginRequest;
import com.oncf.oncf.payload.request.RegisterRequest;
import com.oncf.oncf.payload.response.JwtResponse;
import com.oncf.oncf.security.JwtService;
import com.oncf.oncf.services.UserService;
import com.oncf.oncf.entities.User;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        logger.info("Login attempt for email: {}", loginRequest.getEmail());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String jwt = jwtService.generateToken(userDetails);
            
            // Get the user's role for the response
            Optional<User> user = userService.getUserByEmail(loginRequest.getEmail());
            String role = user.map(u -> u.getRole().toString()).orElse(null);
            
            logger.info("Login successful for email: {}", loginRequest.getEmail());
            return ResponseEntity.ok(new JwtResponse(jwt, role));
        } catch (Exception e) {
            logger.error("Login failed for email: {}, error: {}", loginRequest.getEmail(), e.getMessage());
            return ResponseEntity.badRequest().body("Authentication failed: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            // Check if email is already in use
            if (userService.getUserByEmail(registerRequest.getEmail()).isPresent()) {
                return ResponseEntity.badRequest().body("Error: Email is already in use!");
            }

            // Validate role
            String roleStr = registerRequest.getRole().toUpperCase();
            if (!roleStr.equals("ENGINEER") && !roleStr.equals("TECHNICIAN")) {
                return ResponseEntity.badRequest().body("Error: Role must be either ENGINEER or TECHNICIAN!");
            }

            User.Role role = User.Role.valueOf(roleStr);

            // Create new user
            User user = new User();
            user.setFirstName(registerRequest.getFirstName());
            user.setLastName(registerRequest.getLastName());
            user.setEmail(registerRequest.getEmail());
            user.setPassword(registerRequest.getPassword()); // UserService will encode the password
            user.setPhoneNumber(registerRequest.getPhoneNumber());
            user.setRole(role);

            // Handle supervisor assignment for Technicians
            if (role == User.Role.TECHNICIAN) {
                if (registerRequest.getSupervisorId() == null) {
                    return ResponseEntity.badRequest().body("Error: Supervisor ID is required for Technicians!");
                }

                Optional<User> supervisor = userService.getUserById(registerRequest.getSupervisorId());
                if (supervisor.isEmpty()) {
                    return ResponseEntity.badRequest().body("Error: Supervisor not found!");
                }

                if (supervisor.get().getRole() != User.Role.ENGINEER) {
                    return ResponseEntity.badRequest().body("Error: Technician's supervisor must be an Engineer!");
                }

                user.setSupervisor(supervisor.get());
            }

            // Save the user
            userService.saveUser(user);
            
            logger.info("User registered successfully: {}", registerRequest.getEmail());
            return ResponseEntity.ok("User registered successfully!");
            
        } catch (Exception e) {
            logger.error("Registration failed for email: {}, error: {}", registerRequest.getEmail(), e.getMessage());
            return ResponseEntity.badRequest().body("Registration failed: " + e.getMessage());
        }
    }
    
    @GetMapping("/test")
    public ResponseEntity<?> testEndpoint() {
        logger.info("Test endpoint called");
        return ResponseEntity.ok("API is working!");
    }
    
    @GetMapping("/verify")
    public ResponseEntity<?> verifyToken(@RequestHeader("Authorization") String authHeader) {
        logger.info("Token verification request received");
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.warn("Invalid Authorization header format");
                return ResponseEntity.status(401).body("Invalid token format");
            }
            
            String jwt = authHeader.substring(7);
            String username = jwtService.extractUsername(jwt);
            
            if (username == null) {
                logger.warn("Could not extract username from token");
                return ResponseEntity.status(401).body("Invalid token");
            }
            
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            if (jwtService.isTokenValid(jwt, userDetails)) {
                // Get the user's role and other info
                Optional<User> user = userService.getUserByEmail(username);
                if (user.isPresent()) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("valid", true);
                    response.put("email", username);
                    response.put("role", user.get().getRole().toString());
                    response.put("name", user.get().getFirstName() + " " + user.get().getLastName());
                    
                    logger.info("Token verified successfully for user: {}", username);
                    return ResponseEntity.ok(response);
                }
            }
            
            logger.warn("Token validation failed");
            return ResponseEntity.status(401).body("Invalid token");
        } catch (Exception e) {
            logger.error("Token verification error: {}", e.getMessage());
            return ResponseEntity.status(500).body("Error verifying token: " + e.getMessage());
        }
    }
} 