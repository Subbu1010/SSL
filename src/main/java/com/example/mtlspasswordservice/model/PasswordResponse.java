package com.example.mtlspasswordservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class PasswordResponse {

    @JsonProperty("username")
    private String username;

    @JsonProperty("password")
    private String password;

    @JsonProperty("domain")
    private String domain;

    @JsonProperty("requestId")
    private String requestId;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("status")
    private String status;

    @JsonProperty("message")
    private String message;

    // Default constructor
    public PasswordResponse() {
        this.timestamp = LocalDateTime.now();
    }

    // Constructor with basic fields
    public PasswordResponse(String username, String password, String status) {
        this.username = username;
        this.password = password;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }

    // Constructor with all fields
    public PasswordResponse(String username, String password, String domain, 
                          String requestId, String status, String message) {
        this.username = username;
        this.password = password;
        this.domain = domain;
        this.requestId = requestId;
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "PasswordResponse{" +
                "username='" + username + '\'' +
                ", domain='" + domain + '\'' +
                ", requestId='" + requestId + '\'' +
                ", timestamp=" + timestamp +
                ", status='" + status + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}