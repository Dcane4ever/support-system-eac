package com.cusservice.bsit.controller;

import com.cusservice.bsit.model.User;
import com.cusservice.bsit.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String home(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            String username = auth.getName();
            model.addAttribute("username", username);
            model.addAttribute("isLoggedIn", true);
        } else {
            model.addAttribute("isLoggedIn", false);
        }
        return "landing";
    }

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error,
                       @RequestParam(required = false) String logout,
                       Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid username or password");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully");
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                          @RequestParam String email,
                          @RequestParam String password,
                          @RequestParam String fullName,
                          @RequestParam(required = false) String studentId,
                          @RequestParam String role,
                          Model model) {
        try {
            User.Role userRole = User.Role.valueOf(role.toUpperCase());
            userService.registerUser(username, email, password, userRole, fullName, studentId);
            return "redirect:/login?registered=true";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("user", new User());
            return "register";
        }
    }

    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam String token, Model model) {
        boolean verified = userService.verifyEmail(token);
        if (verified) {
            model.addAttribute("message", "Email verified successfully! You can now log in.");
            return "redirect:/login?verified=true";
        } else {
            model.addAttribute("error", "Invalid or expired verification token");
            return "verify-result";
        }
    }

    @GetMapping("/resend-verification")
    public String resendVerificationForm() {
        return "resend-verification";
    }

    @PostMapping("/resend-verification")
    public String resendVerification(@RequestParam String email, Model model) {
        try {
            userService.resendVerificationEmail(email);
            model.addAttribute("message", "Verification email sent! Please check your inbox.");
        } catch (Exception e) {
            model.addAttribute("error", "Failed to send verification email");
        }
        return "resend-verification";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            User user = userService.findByUsername(username).orElse(null);
            
            if (user != null) {
                model.addAttribute("user", user);
                
                switch (user.getRole()) {
                    case STUDENT:
                        return "redirect:/student/dashboard";
                    case TEACHER:
                        return "redirect:/teacher/dashboard";
                    case SUPPORT_AGENT:
                        return "redirect:/support/dashboard";
                    case ADMIN:
                        return "redirect:/admin/dashboard";
                }
            }
        }
        return "redirect:/login";
    }
}
