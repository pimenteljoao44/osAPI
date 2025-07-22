package com.joao.osMarmoraria.gateway.dto;

import com.joao.osMarmoraria.domain.enums.PaymentStatus;

/**
 * Data Transfer Object for webhook processing results.
 */
public class WebhookResult {
    
    /**
     * Whether the webhook was processed successfully
     */
    private boolean success;
    
    /**
     * Transaction ID related to the webhook
     */
    private String transactionId;
    
    /**
     * New payment status (if status changed)
     */
    private PaymentStatus newStatus;
    
    /**
     * Processing message
     */
    private String message;
    
    /**
     * Whether the webhook should be acknowledged
     */
    private boolean acknowledge = true;
    
    // Constructors
    public WebhookResult() {}
    
    public WebhookResult(boolean success, String transactionId) {
        this.success = success;
        this.transactionId = transactionId;
    }
    
    // Static factory methods
    public static WebhookResult success(String transactionId, PaymentStatus newStatus) {
        WebhookResult result = new WebhookResult(true, transactionId);
        result.setNewStatus(newStatus);
        return result;
    }
    
    public static WebhookResult failed(String transactionId, String message) {
        WebhookResult result = new WebhookResult(false, transactionId);
        result.setMessage(message);
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
    
    public PaymentStatus getNewStatus() {
        return newStatus;
    }
    
    public void setNewStatus(PaymentStatus newStatus) {
        this.newStatus = newStatus;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public boolean isAcknowledge() {
        return acknowledge;
    }
    
    public void setAcknowledge(boolean acknowledge) {
        this.acknowledge = acknowledge;
    }
    
    @Override
    public String toString() {
        return "WebhookResult{" +
                "success=" + success +
                ", transactionId='" + transactionId + '\'' +
                ", newStatus=" + newStatus +
                ", message='" + message + '\'' +
                ", acknowledge=" + acknowledge +
                '}';
    }
}

