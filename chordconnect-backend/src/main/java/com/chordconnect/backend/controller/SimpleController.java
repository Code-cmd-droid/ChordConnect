package com.chordconnect.backend.controller;

import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class SimpleController {

    @GetMapping("/health")
    public String health() {
        return "Backend is running";
    }

    @PostMapping("/debug/request")
    public Map<String, Object> debugRequest(@RequestBody Map<String, Object> request,
                                            @RequestHeader Map<String, String> headers) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("receivedBody", request);
        response.put("receivedHeaders", headers);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
}