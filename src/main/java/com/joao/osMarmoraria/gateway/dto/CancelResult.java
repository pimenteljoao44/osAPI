package com.joao.osMarmoraria.gateway.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object for payment cancellation results.
 */
public class CancelResult {
    
    /**
     * Whether the cancellation was successful
     */
    private boolean success;
    
    /**
     * Transaction ID that was cancelled
     */
    private String transactionId;
    
    /**
     * Cancellation status message
     */
    private String statusMessage;
    
    /**
     * Timestamp when the cancellation was processed
     */
    private LocalDateTime cancelledAt;
    
    /**
     * List of errors that occurred during cancellation
     */
    private List<PaymentError> errors = new ArrayList<>();
    
    // Constructors
    public CancelResult() {}
    
    public CancelResult(boolean success, String transactionId) {
        this.success = success;
        this.transactionId = transactionId;
        this.cancelledAt = LocalDateTime.now();
    }
    
    // Static factory methods
    public static CancelResult success(String transactionId) {
        return new CancelResult(true, transactionId);
    }
    
    public static CancelResult failed(String transactionId, String errorMessage) {
        CancelResult result = new CancelResult(false, transactionId);
        result.setStatusMessage(errorMessage);
        result.addError(new PaymentError("CANCELLATION_FAILED", errorMessage));
        return result;
    }
    
    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    
    public String getStatusMessage() {
        return statusMessage;
    }
    
    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }
    
    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }
    
    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }
    
    public List<PaymentError> getErrors() {
        return errors;
    }
    
    public void setErrors(List<PaymentError> errors) {
        this.errors = errors;
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
    
    @Override
    public String toString() {
        return "CancelResult{" +
                "success=" + success +
                ", transactionId='" + transactionId + '\'' +
                ", statusMessage='" + statusMessage + '\'' +
                ", cancelledAt=" + cancelledAt +
                '}';
    }
}

