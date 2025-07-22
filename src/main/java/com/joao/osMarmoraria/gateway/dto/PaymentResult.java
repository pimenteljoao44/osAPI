package com.joao.osMarmoraria.gateway.dto;

import com.joao.osMarmoraria.domain.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object for payment processing results.
 * Contains the outcome of a payment processing request.
 */
public class PaymentResult {
    
    /**
     * Whether the payment processing was successful
     */
    private boolean success;
    
    /**
     * Current status of the payment
     */
    private PaymentStatus status;
    
    /**
     * Internal transaction identifier
     */
    private String transactionId;
    
    /**
     * Provider-specific transaction identifier
     */
    private String providerTransactionId;
    
    /**
     * Original requested amount
     */
    private BigDecimal requestedAmount;
    
    /**
     * Actually processed amount
     */
    private BigDecimal processedAmount;
    
    /**
     * Currency code
     */
    private String currency;
    
    /**
     * Processing fees charged
     */
    private BigDecimal fees;
    
    /**
     * Net amount (processed amount minus fees)
     */
    private BigDecimal netAmount;
    
    /**
     * Timestamp when the payment was processed
     */
    private LocalDateTime processedAt;
    
    /**
     * Status message from the provider
     */
    private String statusMessage;
    
    /**
     * Payment receipt information
     */
    private PaymentReceipt receipt;
    
    /**
     * Provider-specific response data
     */
    private Map<String, Object> providerResponse = new HashMap<>();
    
    /**
     * List of errors that occurred during processing
     */
    private List<PaymentError> errors = new ArrayList<>();
    
    /**
     * Additional metadata
     */
    private Map<String, String> metadata = new HashMap<>();
    
    /**
     * Payment link or QR code (for applicable payment methods)
     */
    private String paymentLink;
    
    /**
     * QR code data (for PIX payments)
     */
    private String qrCodeData;
    
    /**
     * Expiration time for the payment (if applicable)
     */
    private LocalDateTime expiresAt;
    
    /**
     * Next action required from the user (if any)
     */
    private String nextAction;
    
    /**
     * Redirect URL for completing the payment
     */
    private String redirectUrl;
    
    // Constructors
    public PaymentResult() {}
    
    public PaymentResult(boolean success, PaymentStatus status, String transactionId) {
        this.success = success;
        this.status = status;
        this.transactionId = transactionId;
        this.processedAt = LocalDateTime.now();
    }
    
    // Static factory methods for common scenarios
    public static PaymentResult success(String transactionId, String providerTransactionId, 
                                      BigDecimal processedAmount, String currency) {
        PaymentResult result = new PaymentResult(true, PaymentStatus.COMPLETED, transactionId);
        result.setProviderTransactionId(providerTransactionId);
        result.setProcessedAmount(processedAmount);
        result.setCurrency(currency);
        return result;
    }
    
    public static PaymentResult pending(String transactionId, String statusMessage) {
        PaymentResult result = new PaymentResult(true, PaymentStatus.PENDING, transactionId);
        result.setStatusMessage(statusMessage);
        return result;
    }
    
    public static PaymentResult failed(String transactionId, String errorMessage) {
        PaymentResult result = new PaymentResult(false, PaymentStatus.FAILED, transactionId);
        result.setStatusMessage(errorMessage);
        result.addError(new PaymentError("PAYMENT_FAILED", errorMessage));
        return result;
    }
    
    public static PaymentResult requiresAction(String transactionId, String nextAction, String redirectUrl) {
        PaymentResult result = new PaymentResult(true, PaymentStatus.REQUIRES_ACTION, transactionId);
        result.setNextAction(nextAction);
        result.setRedirectUrl(redirectUrl);
        return result;
    }
    
    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public PaymentStatus getStatus() {
        return status;
    }
    
    public void setStatus(PaymentStatus status) {
        this.status = status;
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
    
    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }
    
    public void setRequestedAmount(BigDecimal requestedAmount) {
        this.requestedAmount = requestedAmount;
    }
    
    public BigDecimal getProcessedAmount() {
        return processedAmount;
    }
    
    public void setProcessedAmount(BigDecimal processedAmount) {
        this.processedAmount = processedAmount;
        // Calculate net amount if fees are available
        if (this.fees != null) {
            this.netAmount = processedAmount.subtract(this.fees);
        }
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
        // Recalculate net amount if processed amount is available
        if (this.processedAmount != null) {
            this.netAmount = this.processedAmount.subtract(fees);
        }
    }
    
    public BigDecimal getNetAmount() {
        return netAmount;
    }
    
    public void setNetAmount(BigDecimal netAmount) {
        this.netAmount = netAmount;
    }
    
    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
    
    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
    
    public String getStatusMessage() {
        return statusMessage;
    }
    
    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }
    
    public PaymentReceipt getReceipt() {
        return receipt;
    }
    
    public void setReceipt(PaymentReceipt receipt) {
        this.receipt = receipt;
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
    
    public String getPaymentLink() {
        return paymentLink;
    }
    
    public void setPaymentLink(String paymentLink) {
        this.paymentLink = paymentLink;
    }
    
    public String getQrCodeData() {
        return qrCodeData;
    }
    
    public void setQrCodeData(String qrCodeData) {
        this.qrCodeData = qrCodeData;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public String getNextAction() {
        return nextAction;
    }
    
    public void setNextAction(String nextAction) {
        this.nextAction = nextAction;
    }
    
    public String getRedirectUrl() {
        return redirectUrl;
    }
    
    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
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
    public boolean isPaymentSuccessful() {
        return success && status == PaymentStatus.COMPLETED;
    }
    
    @Override
    public String toString() {
        return "PaymentResult{" +
                "success=" + success +
                ", status=" + status +
                ", transactionId='" + transactionId + '\'' +
                ", providerTransactionId='" + providerTransactionId + '\'' +
                ", processedAmount=" + processedAmount +
                ", currency='" + currency + '\'' +
                ", processedAt=" + processedAt +
                '}';
    }
}

