package com.example.mtlspasswordservice.service;

import com.example.mtlspasswordservice.model.PasswordRequest;
import com.example.mtlspasswordservice.model.PasswordResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Service
public class ExternalPasswordService {

    private static final Logger logger = LoggerFactory.getLogger(ExternalPasswordService.class);

    private final RestTemplate restTemplate;

    @Value("${external.api.base-url}")
    private String externalApiBaseUrl;

    @Value("${external.api.endpoint:/api/v1/password}")
    private String passwordEndpoint;

    @Value("${external.api.timeout:30}")
    private int timeoutSeconds;

    public ExternalPasswordService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public PasswordResponse getUserPassword(PasswordRequest request) {
        logger.info("Requesting password for user: {}", request.getUsername());

        try {
            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

            // Create HTTP entity with request body and headers
            HttpEntity<PasswordRequest> entity = new HttpEntity<>(request, headers);

            // Make the API call
            String url = externalApiBaseUrl + passwordEndpoint;
            ResponseEntity<PasswordResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    PasswordResponse.class
            );

            logger.info("Successfully retrieved password for user: {}", request.getUsername());
            return response.getBody();

        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            logger.error("HTTP error calling external API: {} - {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new RuntimeException("Failed to retrieve password from external service: " + ex.getStatusCode(), ex);
        } catch (ResourceAccessException ex) {
            logger.error("Connection error calling external API for user: {}", request.getUsername(), ex);
            throw new RuntimeException("Connection error while calling external service", ex);
        } catch (Exception ex) {
            logger.error("Unexpected error calling external API for user: {}", request.getUsername(), ex);
            throw new RuntimeException("Unexpected error while calling external service", ex);
        }
    }

    public String healthCheck() {
        logger.info("Performing health check on external API");

        try {
            String url = externalApiBaseUrl + "/health";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            logger.info("External API health check successful");
            return response.getBody();
            
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            logger.error("HTTP error during health check: {} - {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            return "Health check failed: " + ex.getStatusCode();
        } catch (ResourceAccessException ex) {
            logger.error("Connection error during health check", ex);
            return "Health check failed: Connection error";
        } catch (Exception ex) {
            logger.error("Unexpected error during health check", ex);
            return "Health check failed: " + ex.getMessage();
        }
    }
}