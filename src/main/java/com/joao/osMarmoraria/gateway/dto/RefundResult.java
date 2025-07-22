package com.joao.osMarmoraria.gateway.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object for refund processing results.
 */
public class RefundResult {
    
    /**
     * Whether the refund was successful
     */
    private boolean success;
    
    /**
     * Original transaction ID
     */
    private String originalTransactionId;
    
    /**
     * Refund transaction ID
     */
    private String refundTransactionId;
    
    /**
     * Provider-specific refund ID
     */
    private String providerRefundId;
    
    /**
     * Refunded amount
     */
    private BigDecimal refundedAmount;
    
    /**
     * Currency code
     */
    private String currency;
    
    /**
     * Refund status message
     */
    private String statusMessage;
    
    /**
     * Timestamp when the refund was processed
     */
    private LocalDateTime processedAt;
    
    /**
     * Expected time for refund to appear in customer's account
     */
    private LocalDateTime expectedRefundDate;
    
    /**
     * Provider-specific response data
     */
    private Map<String, Object> providerResponse = new HashMap<>();
    
    /**
     * List of errors that occurred during refund processing
     */
    private List<PaymentError> errors = new ArrayList<>();
    
    /**
     * Additional metadata
     */
    private Map<String, String> metadata = new HashMap<>();
    
    // Constructors
    public RefundResult() {}
    
    public RefundResult(boolean success, String originalTransactionId, String refundTransactionId) {
        this.success = success;
        this.originalTransactionId = originalTransactionId;
        this.refundTransactionId = refundTransactionId;
        this.processedAt = LocalDateTime.now();
    }
    
    // Static factory methods
    public static RefundResult success(String originalTransactionId, String refundTransactionId, 
                                     BigDecimal refundedAmount, String currency) {
        RefundResult result = new RefundResult(true, originalTransactionId, refundTransactionId);
        result.setRefundedAmount(refundedAmount);
        result.setCurrency(currency);
        return result;
    }
    
    public static RefundResult failed(String originalTransactionId, String errorMessage) {
        RefundResult result = new RefundResult(false, originalTransactionId, null);
        result.setStatusMessage(errorMessage);
        result.addError(new PaymentError("REFUND_FAILED", errorMessage));
        return result;
    }
    
    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getOriginalTransactionId() {
        return originalTransactionId;
    }
    
    public void setOriginalTransactionId(String originalTransactionId) {
        this.originalTransactionId = originalTransactionId;
    }
    
    public String getRefundTransactionId() {
        return refundTransactionId;
    }
    
    public void setRefundTransactionId(String refundTransactionId) {
        this.refundTransactionId = refundTransactionId;
    }
    
    public String getProviderRefundId() {
        return providerRefundId;
    }
    
    public void setProviderRefundId(String providerRefundId) {
        this.providerRefundId = providerRefundId;
    }
    
    public BigDecimal getRefundedAmount() {
        return refundedAmount;
    }
    
    public void setRefundedAmount(BigDecimal refundedAmount) {
        this.refundedAmount = refundedAmount;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public String getStatusMessage() {
        return statusMessage;
    }
    
    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }
    
    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
    
    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
    
    public LocalDateTime getExpectedRefundDate() {
        return expectedRefundDate;
    }
    
    public void setExpectedRefundDate(LocalDateTime expectedRefundDate) {
        this.expectedRefundDate = expectedRefundDate;
    }
    
    public Map<String, Object> getProviderResponse() {
        return providerResponse;
    }
    
    public void setProviderResponse(Map<String, Object> providerResponse) {
        this.providerResponse = providerResponse;
    }
    
    public List<PaymentError> getErrors() {
        return errors;
    }
    
    public void setErrors(List<PaymentError> errors) {
        this.errors = errors;
    }
    
    public Map<String, String> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
    
    // Utility methods
    
    /**
     * Add an error to the result
     * @param error PaymentError to add
     */
    public void addError(PaymentError error) {
        this.errors.add(error);
        this.success = false;
    }
    
    /**
     * Add an error with code and message
     * @param code error code
     * @param message error message
     */
    public void addError(String code, String message) {
        this.addError(new PaymentError(code, message));
    }
    
    /**
     * Check if there are any errors
     * @return true if errors exist
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
    
    /**
     * Add provider response data
     * @param key data key
     * @param value data value
     */
    public void addProviderResponse(String key, Object value) {
        this.providerResponse.put(key, value);
    }
    
    /**
     * Add metadata entry
     * @param key metadata key
     * @param value metadata value
     */
    public void addMetadata(String key, String value) {
        this.metadata.put(key, value);
    }
    
    @Override
    public String toString() {
        return "RefundResult{" +
                "success=" + success +
                ", originalTransactionId='" + originalTransactionId + '\'' +
                ", refundTransactionId='" + refundTransactionId + '\'' +
                ", refundedAmount=" + refundedAmount +
                ", currency='" + currency + '\'' +
                ", processedAt=" + processedAt +
                '}';
    }
}

