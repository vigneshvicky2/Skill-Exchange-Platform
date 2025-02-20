package com.vicky.vignesh.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;


import com.vicky.vignesh.model.User;
import com.vicky.vignesh.controller.EmailSenderService;
import com.vicky.vignesh.service.UserService;

@RestController
@RequestMapping("/auth")
public class PasswordResetController {



    @Autowired
    private EmailSenderService emailSenderService;

    @Autowired
    private UserService userService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/reset-password")
public ResponseEntity<String> resetPassword(@RequestBody PasswordResetRequest request) {
    Optional<User> userOptional = userService.findByEmail(request.getEmail());
    if (userOptional.isPresent()) {
        User user = userOptional.get();
        user.setPassword(passwordEncoder.encode(request.getNewPassword())); // Encode the new password
        userService.save(user); // Update the user in the database
        return ResponseEntity.ok("Password has been reset successfully!");
    } else {
        return ResponseEntity.status(404).body("User with the provided email not found");
    }
}


    @PostMapping("/send-email")
    public ResponseEntity<String> sendEmail(@RequestBody EmailRequest emailRequest) {
        try {
            emailSenderService.sendSimpleEmail(emailRequest.getToEmail(), emailRequest.getSubject(), emailRequest.getBody());
            return ResponseEntity.ok("Email sent successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error sending email: " + e.getMessage());
        }
    }
}

class EmailRequest {
    private String toEmail;
    private String subject;
    private String body;

    // Getters and Setters
    public String getToEmail() {
        return toEmail;
    }

    public void setToEmail(String toEmail) {
        this.toEmail = toEmail;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
class PasswordResetRequest {
    private String email;
    private String newPassword;

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
