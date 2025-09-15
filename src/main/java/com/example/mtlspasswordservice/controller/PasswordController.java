package com.example.mtlspasswordservice.controller;

import com.example.mtlspasswordservice.model.PasswordRequest;
import com.example.mtlspasswordservice.model.PasswordResponse;
import com.example.mtlspasswordservice.service.ExternalPasswordService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class PasswordController {

    private static final Logger logger = LoggerFactory.getLogger(PasswordController.class);

    private final ExternalPasswordService externalPasswordService;

    public PasswordController(ExternalPasswordService externalPasswordService) {
        this.externalPasswordService = externalPasswordService;
    }

    @PostMapping(value = "/password", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PasswordResponse> getUserPassword(@Valid @RequestBody PasswordRequest request) {
        logger.info("Received password request for user: {}", request.getUsername());

        // Generate request ID if not provided
        if (request.getRequestId() == null || request.getRequestId().isEmpty()) {
            request.setRequestId(UUID.randomUUID().toString());
        }

        try {
            PasswordResponse response = externalPasswordService.getUserPassword(request);
            logger.info("Successfully processed password request for user: {} with requestId: {}", 
                    request.getUsername(), request.getRequestId());
            return ResponseEntity.ok(response);
            
        } catch (Exception error) {
            logger.error("Error processing password request for user: {} with requestId: {}", 
                    request.getUsername(), request.getRequestId(), error);
            
            PasswordResponse errorResponse = new PasswordResponse();
            errorResponse.setUsername(request.getUsername());
            errorResponse.setRequestId(request.getRequestId());
            errorResponse.setStatus("ERROR");
            errorResponse.setMessage("Failed to retrieve password: " + error.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> healthCheck() {
        logger.info("Health check endpoint called");
        
        try {
            String healthStatus = externalPasswordService.healthCheck();
            if (healthStatus.contains("Health check failed")) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(healthStatus);
            }
            return ResponseEntity.ok(healthStatus);
            
        } catch (Exception error) {
            logger.error("Health check failed", error);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Health check failed: " + error.getMessage());
        }
    }

    @GetMapping(value = "/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> status() {
        logger.info("Status endpoint called");
        return ResponseEntity.ok("{\"status\":\"UP\",\"service\":\"MTLS Password Service\"}");
    }
}