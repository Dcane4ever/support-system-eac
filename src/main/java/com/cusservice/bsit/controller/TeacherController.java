package com.cusservice.bsit.controller;

import com.cusservice.bsit.model.User;
import com.cusservice.bsit.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/teacher")
public class TeacherController {

    private final UserService userService;

    public TeacherController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        String username = authentication.getName();
        User user = userService.findByUsername(username).orElse(null);
        model.addAttribute("user", user);
        return "teacher-dashboard";
    }

    @GetMapping("/chat")
    public String chat(Authentication authentication, Model model) {
        String username = authentication.getName();
        User user = userService.findByUsername(username).orElse(null);
        model.addAttribute("user", user);
        return "teacher-chat";
    }
}
