package com.cusservice.bsit.service;

import com.cusservice.bsit.model.User;
import com.cusservice.bsit.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Transactional
    public User registerUser(String username, String email, String password, User.Role role, String fullName, String studentId) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setFullName(fullName);
        user.setStudentId(studentId);
        user.setEmailVerified(false);
        user.setVerificationToken(UUID.randomUUID().toString());
        user.setTokenExpiryDate(LocalDateTime.now().plusHours(24));

        User savedUser = userRepository.save(user);

        // Send verification email
        try {
            emailService.sendVerificationEmail(savedUser);
        } catch (Exception e) {
            // Log error but don't fail registration
            System.err.println("Failed to send verification email: " + e.getMessage());
        }

        return savedUser;
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public boolean verifyEmail(String token) {
        Optional<User> userOpt = userRepository.findByVerificationToken(token);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getTokenExpiryDate().isAfter(LocalDateTime.now())) {
                user.setEmailVerified(true);
                user.setVerificationToken(null);
                user.setTokenExpiryDate(null);
                userRepository.save(user);
                return true;
            }
        }
        return false;
    }

    @Transactional
    public void resendVerificationEmail(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (!user.isEmailVerified()) {
                user.setVerificationToken(UUID.randomUUID().toString());
                user.setTokenExpiryDate(LocalDateTime.now().plusHours(24));
                userRepository.save(user);
                emailService.sendVerificationEmail(user);
            }
        }
    }
    
    /**
     * Update agent availability status
     */
    @Transactional
    public void updateAgentAvailability(Long agentId, boolean available) {
        Optional<User> userOpt = userRepository.findById(agentId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setAvailable(available);
            userRepository.save(user);
        }
    }
}
