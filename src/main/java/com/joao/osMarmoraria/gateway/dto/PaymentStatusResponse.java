package com.joao.osMarmoraria.gateway.dto;

import com.joao.osMarmoraria.domain.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Data Transfer Object for payment status query responses.
 */
public class PaymentStatusResponse {
    
    /**
     * Transaction ID
     */
    private String transactionId;
    
    /**
     * Provider-specific transaction ID
     */
    private String providerTransactionId;
    
    /**
     * Current payment status
     */
    private PaymentStatus status;
    
    /**
     * Status message
     */
    private String statusMessage;
    
    /**
     * Original payment amount
     */
    private BigDecimal amount;
    
    /**
     * Processed amount
     */
    private BigDecimal processedAmount;
    
    /**
     * Currency code
     */
    private String currency;
    
    /**
     * Processing fees
     */
    private BigDecimal fees;
    
    /**
     * Timestamp when the payment was created
     */
    private LocalDateTime createdAt;
    
    /**
     * Timestamp when the payment was last updated
     */
    private LocalDateTime updatedAt;
    
    /**
     * Timestamp when the payment was processed
     */
    private LocalDateTime processedAt;
    
    /**
     * Expiration timestamp (if applicable)
     */
    private LocalDateTime expiresAt;
    
    /**
     * Provider-specific response data
     */
    private Map<String, Object> providerData = new HashMap<>();
    
    /**
     * Additional metadata
     */
    private Map<String, String> metadata = new HashMap<>();
    
    // Constructors
    public PaymentStatusResponse() {}
    
    public PaymentStatusResponse(String transactionId, PaymentStatus status) {
        this.transactionId = transactionId;
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
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
    
    public PaymentStatus getStatus() {
        return status;
    }
    
    public void setStatus(PaymentStatus status) {
        this.status = status;
    }
    
    public String getStatusMessage() {
        return statusMessage;
    }
    
    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public BigDecimal getProcessedAmount() {
        return processedAmount;
    }
    
    public void setProcessedAmount(BigDecimal processedAmount) {
        this.processedAmount = processedAmount;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public BigDecimal getFees() {
        return fees;
    }
    
    public void setFees(BigDecimal fees) {
        this.fees = fees;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
    
    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public Map<String, Object> getProviderData() {
        return providerData;
    }
    
    public void setProviderData(Map<String, Object> providerData) {
        this.providerData = providerData;
    }
    
    public Map<String, String> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
    
    // Utility methods
    
    /**
     * Add provider data
     * @param key data key
     * @param value data value
     */
    public void addProviderData(String key, Object value) {
        this.providerData.put(key, value);
    }
    
    /**
     * Add metadata entry
     * @param key metadata key
     * @param value metadata value
     */
    public void addMetadata(String key, String value) {
        this.metadata.put(key, value);
    }
    
    /**
     * Check if the payment is in a final state
     * @return true if the payment is in a final state
     */
    public boolean isFinalState() {
        return status != null && status.isFinalStatus();
    }
    
    /**
     * Check if the payment was successful
     * @return true if the payment was successful
     */
    public boolean isSuccessful() {
        return status != null && status.isSuccessful();
    }
    
    /**
     * Check if the payment is expired
     * @return true if the payment is expired
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
    
    @Override
    public String toString() {
        return "PaymentStatusResponse{" +
                "transactionId='" + transactionId + '\'' +
                ", providerTransactionId='" + providerTransactionId + '\'' +
                ", status=" + status +
                ", amount=" + amount +
                ", processedAmount=" + processedAmount +
                ", currency='" + currency + '\'' +
                ", updatedAt=" + updatedAt +
                '}';
    }
}

