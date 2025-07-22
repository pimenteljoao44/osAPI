package com.joao.osMarmoraria.gateway.dto;

import com.joao.osMarmoraria.domain.enums.PaymentMethod;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Data Transfer Object for payment processing requests.
 * Contains all necessary information to process a payment transaction.
 */
public class PaymentRequest {
    
    /**
     * Unique identifier for this payment request
     */
    @NotNull
    @Size(min = 1, max = 100)
    private String requestId;
    
    /**
     * Payment amount
     */
    @NotNull
    @Positive
    private BigDecimal amount;
    
    /**
     * Currency code (typically BRL)
     */
    @NotNull
    @Size(min = 3, max = 3)
    private String currency = "BRL";
    
    /**
     * Payment method to be used
     */
    @NotNull
    private PaymentMethod paymentMethod;
    
    /**
     * Payment description
     */
    @Size(max = 500)
    private String description;
    
    /**
     * Reference to the related order or invoice
     */
    @Size(max = 100)
    private String orderReference;
    
    /**
     * Customer information
     */
    private CustomerInfo customer;
    
    /**
     * Billing address information
     */
    private BillingAddress billingAddress;
    
    /**
     * Payment-specific options and configurations
     */
    private PaymentOptions options;
    
    /**
     * Callback URLs for payment notifications
     */
    private CallbackUrls callbackUrls;
    
    /**
     * Expiration time for time-sensitive payments
     */
    private LocalDateTime expiresAt;
    
    /**
     * Additional metadata for the payment
     */
    private Map<String, String> metadata = new HashMap<>();
    
    /**
     * Payment method specific data (e.g., card details, PIX key)
     */
    private Map<String, Object> paymentMethodData = new HashMap<>();
    
    // Constructors
    public PaymentRequest() {}
    
    public PaymentRequest(String requestId, BigDecimal amount, PaymentMethod paymentMethod, String description) {
        this.requestId = requestId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.description = description;
    }
    
    // Getters and Setters
    public String getRequestId() {
        return requestId;
    }
    
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getOrderReference() {
        return orderReference;
    }
    
    public void setOrderReference(String orderReference) {
        this.orderReference = orderReference;
    }
    
    public CustomerInfo getCustomer() {
        return customer;
    }
    
    public void setCustomer(CustomerInfo customer) {
        this.customer = customer;
    }
    
    public BillingAddress getBillingAddress() {
        return billingAddress;
    }
    
    public void setBillingAddress(BillingAddress billingAddress) {
        this.billingAddress = billingAddress;
    }
    
    public PaymentOptions getOptions() {
        return options;
    }
    
    public void setOptions(PaymentOptions options) {
        this.options = options;
    }
    
    public CallbackUrls getCallbackUrls() {
        return callbackUrls;
    }
    
    public void setCallbackUrls(CallbackUrls callbackUrls) {
        this.callbackUrls = callbackUrls;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public Map<String, String> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
    
    public Map<String, Object> getPaymentMethodData() {
        return paymentMethodData;
    }
    
    public void setPaymentMethodData(Map<String, Object> paymentMethodData) {
        this.paymentMethodData = paymentMethodData;
    }
    
    // Utility methods
    
    /**
     * Add metadata entry
     * @param key metadata key
     * @param value metadata value
     */
    public void addMetadata(String key, String value) {
        this.metadata.put(key, value);
    }
    
    /**
     * Get metadata value by key
     * @param key metadata key
     * @return metadata value or null if not found
     */
    public String getMetadata(String key) {
        return this.metadata.get(key);
    }
    
    /**
     * Add payment method specific data
     * @param key data key
     * @param value data value
     */
    public void addPaymentMethodData(String key, Object value) {
        this.paymentMethodData.put(key, value);
    }
    
    /**
     * Get payment method specific data
     * @param key data key
     * @return data value or null if not found
     */
    public Object getPaymentMethodData(String key) {
        return this.paymentMethodData.get(key);
    }
    
    /**
     * Check if the payment request is expired
     * @return true if expired
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
    
    @Override
    public String toString() {
        return "PaymentRequest{" +
                "requestId='" + requestId + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", paymentMethod=" + paymentMethod +
                ", description='" + description + '\'' +
                ", orderReference='" + orderReference + '\'' +
                '}';
    }
}

