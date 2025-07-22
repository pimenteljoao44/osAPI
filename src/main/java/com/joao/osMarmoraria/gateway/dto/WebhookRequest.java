package com.joao.osMarmoraria.gateway.dto;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Data Transfer Object for webhook notification requests.
 */
public class WebhookRequest {
    
    /**
     * Webhook event type
     */
    private String eventType;
    
    /**
     * Transaction ID related to the webhook
     */
    private String transactionId;
    
    /**
     * Provider-specific transaction ID
     */
    private String providerTransactionId;
    
    /**
     * Webhook payload data
     */
    private Map<String, Object> payload = new HashMap<>();
    
    /**
     * Webhook signature for verification
     */
    private String signature;
    
    /**
     * Timestamp when the webhook was sent
     */
    private LocalDateTime timestamp;
    
    /**
     * Webhook ID from the provider
     */
    private String webhookId;
    
    /**
     * Provider ID that sent the webhook
     */
    private String providerId;
    
    // Constructors
    public WebhookRequest() {}
    
    public WebhookRequest(String eventType, String transactionId) {
        this.eventType = eventType;
        this.transactionId = transactionId;
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getEventType() {
        return eventType;
    }
    
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    
    public String getProviderTransactionId() {
        return providerTransactionId;
    }
    
    public void setProviderTransactionId(String providerTransactionId) {
        this.providerTransactionId = providerTransactionId;
    }
    
    public Map<String, Object> getPayload() {
        return payload;
    }
    
    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }
    
    public String getSignature() {
        return signature;
    }
    
    public void setSignature(String signature) {
        this.signature = signature;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getWebhookId() {
        return webhookId;
    }
    
    public void setWebhookId(String webhookId) {
        this.webhookId = webhookId;
    }
    
    public String getProviderId() {
        return providerId;
    }
    
    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }
    
    // Utility methods
    
    /**
     * Add payload data
     * @param key data key
     * @param value data value
     */
    public void addPayloadData(String key, Object value) {
        this.payload.put(key, value);
    }
    
    /**
     * Get payload data by key
     * @param key data key
     * @return data value or null if not found
     */
    public Object getPayloadData(String key) {
        return this.payload.get(key);
    }
    
    @Override
    public String toString() {
        return "WebhookRequest{" +
                "eventType='" + eventType + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", providerTransactionId='" + providerTransactionId + '\'' +
                ", webhookId='" + webhookId + '\'' +
                ", providerId='" + providerId + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}

