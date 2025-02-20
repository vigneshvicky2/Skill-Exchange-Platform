package com.vicky.vignesh.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vicky.vignesh.model.User;
import com.vicky.vignesh.security.JwtUtil;
import com.vicky.vignesh.service.UserService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/signup")
    public ResponseEntity<String> register(@RequestBody User user) {
        userService.register(user);
        return ResponseEntity.ok("User registered successfully!");
    }

    @PostMapping("/signin")
    public ResponseEntity<String> login(@RequestBody User user) {
        Optional<User> existingUser = userService.findByEmail(user.getEmail());
        if (existingUser.isPresent() &&
                passwordEncoder.matches(user.getPassword(), existingUser.get().getPassword())) {
            User userToUpdate = existingUser.get();
            userToUpdate.setLoggedIn(true); // Set isLoggedIn to true
            userService.updateUser(userToUpdate); // Save the updated user

            String token = jwtUtil.generateToken(user.getEmail());
            return ResponseEntity.ok(token);
        } else {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }

    @PostMapping("/signout")
    public ResponseEntity<String> signout(HttpServletRequest request) {
        String email = getEmailFromToken(request); // Corrected method call
        if (email != null) {
            Optional<User> existingUser = userService.findByEmail(email);
            if (existingUser.isPresent()) {
                User userToUpdate = existingUser.get();
                userToUpdate.setLoggedIn(false); // Set isLoggedIn to false
                userService.updateUser(userToUpdate); // Save the updated user

                return ResponseEntity.ok("User signed out successfully");
            } else {
                return ResponseEntity.badRequest().body("User not found");
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // Return unauthorized if email is null
    }

    @PostMapping("/verify")
    public ResponseEntity<String> checkAuth(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            
            if (jwtUtil.validateToken(token)) {
                String email = jwtUtil.extractEmail(token);
                return ResponseEntity.ok("User is authenticated: " + email);
            } else {
                return ResponseEntity.status(401).body("Invalid token");
            }
        }
        return ResponseEntity.status(401).body("Authorization header missing");
    }

    // Helper method to extract email from token
    private String getEmailFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtUtil.extractEmail(token); // Extract email from token
        }
        return null;
    }
}
