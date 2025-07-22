package com.joao.osMarmoraria.gateway.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO para resultado de verificação de saúde de provedores
 */
public class HealthCheckResult {

    private String providerId;
    private String providerName;
    private boolean healthy;
    private String status; // UP, DOWN, DEGRADED
    private LocalDateTime timestamp;
    private Long responseTimeMs;
    private String errorMessage;
    private String errorCode;
    private Map<String, Object> details;
    private Map<String, String> metrics;

    // Construtores
    public HealthCheckResult() {
        this.timestamp = LocalDateTime.now();
    }

    public HealthCheckResult(String providerId, boolean healthy) {
        this();
        this.providerId = providerId;
        this.healthy = healthy;
        this.status = healthy ? "UP" : "DOWN";
    }

    public HealthCheckResult(String providerId, String providerName, boolean healthy) {
        this(providerId, healthy);
        this.providerName = providerName;
    }

    // Getters e Setters
    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public boolean isHealthy() {
        return healthy;
    }

    public void setHealthy(boolean healthy) {
        this.healthy = healthy;
        this.status = healthy ? "UP" : "DOWN";
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        this.healthy = "UP".equalsIgnoreCase(status);
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Long getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(Long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }

    public Map<String, String> getMetrics() {
        return metrics;
    }

    public void setMetrics(Map<String, String> metrics) {
        this.metrics = metrics;
    }

    // Métodos utilitários
    public void addDetail(String key, Object value) {
        if (details == null) {
            details = new java.util.HashMap<>();
        }
        details.put(key, value);
    }

    public void addMetric(String key, String value) {
        if (metrics == null) {
            metrics = new java.util.HashMap<>();
        }
        metrics.put(key, value);
    }

    public boolean isUp() {
        return "UP".equalsIgnoreCase(status);
    }

    public boolean isDown() {
        return "DOWN".equalsIgnoreCase(status);
    }

    public boolean isDegraded() {
        return "DEGRADED".equalsIgnoreCase(status);
    }

    public void markAsDegraded(String reason) {
        this.status = "DEGRADED";
        this.healthy = false;
        this.errorMessage = reason;
    }

    public void markAsDown(String errorMessage) {
        this.status = "DOWN";
        this.healthy = false;
        this.errorMessage = errorMessage;
    }

    public void markAsUp() {
        this.status = "UP";
        this.healthy = true;
        this.errorMessage = null;
        this.errorCode = null;
    }
}

