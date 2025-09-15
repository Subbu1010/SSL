package com.example.mtlspasswordservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PasswordRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 1, max = 100, message = "Username must be between 1 and 100 characters")
    @JsonProperty("username")
    private String username;

    @JsonProperty("domain")
    private String domain;

    @JsonProperty("requestId")
    private String requestId;

    // Default constructor
    public PasswordRequest() {}

    // Constructor with username
    public PasswordRequest(String username) {
        this.username = username;
    }

    // Constructor with all fields
    public PasswordRequest(String username, String domain, String requestId) {
        this.username = username;
        this.domain = domain;
        this.requestId = requestId;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    @Override
    public String toString() {
        return "PasswordRequest{" +
                "username='" + username + '\'' +
                ", domain='" + domain + '\'' +
                ", requestId='" + requestId + '\'' +
                '}';
    }
}