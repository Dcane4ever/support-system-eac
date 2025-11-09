package com.cusservice.bsit.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TurnConfigController {
    
    @Value("${turn.username:default_user}")
    private String turnUsername;
    
    @Value("${turn.credential:default_pass}")
    private String turnCredential;
    
    @GetMapping("/turn-config")
    public Map<String, Object> getTurnConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("username", turnUsername);
        config.put("credential", turnCredential);
        return config;
    }
}
