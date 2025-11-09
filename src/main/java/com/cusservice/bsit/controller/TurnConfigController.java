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
    
    @Value("${metered.api.key:}")
    private String meteredApiKey;
    
    /**
     * Get Metered API configuration for fetching TURN credentials dynamically
     * @return Map containing API key and endpoint URL
     */
    @GetMapping("/turn-config")
    public Map<String, Object> getTurnConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("apiKey", meteredApiKey);
        config.put("endpoint", "https://support-system-eac.metered.live/api/v1/turn/credentials");
        return config;
    }
}
