package com.joao.osMarmoraria.gateway.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Data Transfer Object for refund processing requests.
 */
public class RefundRequest {
    
    /**
     * Original transaction ID to refund
     */
    @NotNull
    @Size(min = 1, max = 100)
    private String transactionId;
    
    /**
     * Refund amount (null for full refund)
     */
    @Positive
    private BigDecimal amount;
    
    /**
     * Reason for the refund
     */
    @Size(max = 500)
    private String reason;
    
    /**
     * Refund reference ID
     */
    @Size(max = 100)
    private String refundReference;
    
    /**
     * Whether to notify the customer
     */
    private Boolean notifyCustomer = true;
    
    // Constructors
    public RefundRequest() {}
    
    public RefundRequest(String transactionId, BigDecimal amount, String reason) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.reason = reason;
    }
    
    // Getters and Setters
    public String getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public String getRefundReference() {
        return refundReference;
    }
    
    public void setRefundReference(String refundReference) {
        this.refundReference = refundReference;
    }
    
    public Boolean getNotifyCustomer() {
        return notifyCustomer;
    }
    
    public void setNotifyCustomer(Boolean notifyCustomer) {
        this.notifyCustomer = notifyCustomer;
    }
    
    @Override
    public String toString() {
        return "RefundRequest{" +
                "transactionId='" + transactionId + '\'' +
                ", amount=" + amount +
                ", reason='" + reason + '\'' +
                ", refundReference='" + refundReference + '\'' +
                '}';
    }
}

