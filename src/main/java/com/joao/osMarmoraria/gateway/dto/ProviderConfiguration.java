package com.joao.osMarmoraria.gateway.dto;

import java.util.Map;

/**
 * DTO para configuração de provedores de pagamento
 */
public class ProviderConfiguration {

    private String providerId;
    private String providerName;
    private boolean enabled;
    private Integer priority;
    private String apiKey;
    private String secretKey;
    private String baseUrl;
    private String webhookUrl;
    private Map<String, String> credentials;
    private Map<String, Object> settings;
    private String environment; // SANDBOX, PRODUCTION
    private Integer timeoutSeconds;
    private Integer maxRetries;
    private boolean enableLogging;

    // Construtores
    public ProviderConfiguration() {}

    public ProviderConfiguration(String providerId, String providerName) {
        this.providerId = providerId;
        this.providerName = providerName;
        this.enabled = true;
        this.priority = 1;
        this.environment = "SANDBOX";
        this.timeoutSeconds = 30;
        this.maxRetries = 3;
        this.enableLogging = true;
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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getWebhookUrl() {
        return webhookUrl;
    }

    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public Map<String, String> getCredentials() {
        return credentials;
    }

    public void setCredentials(Map<String, String> credentials) {
        this.credentials = credentials;
    }

    public Map<String, Object> getSettings() {
        return settings;
    }

    public void setSettings(Map<String, Object> settings) {
        this.settings = settings;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public Integer getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(Integer timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public Integer getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }

    public boolean isEnableLogging() {
        return enableLogging;
    }

    public void setEnableLogging(boolean enableLogging) {
        this.enableLogging = enableLogging;
    }

    // Métodos utilitários
    public String getCredential(String key) {
        return credentials != null ? credentials.get(key) : null;
    }

    public void setCredential(String key, String value) {
        if (credentials == null) {
            credentials = new java.util.HashMap<>();
        }
        credentials.put(key, value);
    }

    public Object getSetting(String key) {
        return settings != null ? settings.get(key) : null;
    }

    public void setSetting(String key, Object value) {
        if (settings == null) {
            settings = new java.util.HashMap<>();
        }
        settings.put(key, value);
    }

    public boolean isProduction() {
        return "PRODUCTION".equalsIgnoreCase(environment);
    }

    public boolean isSandbox() {
        return "SANDBOX".equalsIgnoreCase(environment);
    }
}

