package com.joao.osMarmoraria.domain;

import com.joao.osMarmoraria.domain.enums.PaymentMethod;
import com.joao.osMarmoraria.domain.enums.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Entity representing a payment transaction in the payment gateway abstraction layer.
 * This entity stores all information related to a payment transaction including
 * provider-specific data, status tracking, and audit information.
 */
@Entity
@Table(name = "payment_transactions")
public class PaymentTransaction implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Unique transaction identifier for this system
     */
    @Column(name = "transaction_id", unique = true, nullable = false)
    private String transactionId;
    
    /**
     * Provider-specific transaction identifier
     */
    @Column(name = "provider_transaction_id")
    private String providerTransactionId;
    
    /**
     * Payment provider identifier
     */
    @Column(name = "provider_id", nullable = false)
    private String providerId;
    
    /**
     * Payment method used for this transaction
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;
    
    /**
     * Current status of the payment transaction
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;
    
    /**
     * Original amount requested for payment
     */
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    
    /**
     * Actually processed amount (may differ from requested amount)
     */
    @Column(name = "processed_amount", precision = 19, scale = 2)
    private BigDecimal processedAmount;
    
    /**
     * Currency code (typically BRL for Brazilian Real)
     */
    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "BRL";
    
    /**
     * Processing fees charged by the provider
     */
    @Column(name = "fees", precision = 19, scale = 2)
    private BigDecimal fees;
    
    /**
     * Transaction description
     */
    @Column(name = "description", length = 500)
    private String description;
    
    /**
     * Reference to the related order or invoice
     */
    @Column(name = "order_reference")
    private String orderReference;
    
    /**
     * Customer information (JSON format for flexibility)
     */
    @Column(name = "customer_info", columnDefinition = "TEXT")
    private String customerInfo;
    
    /**
     * Provider-specific response data (JSON format)
     */
    @Column(name = "provider_response", columnDefinition = "TEXT")
    private String providerResponse;
    
    /**
     * Error message if the transaction failed
     */
    @Column(name = "error_message", length = 1000)
    private String errorMessage;
    
    /**
     * Error code from the provider
     */
    @Column(name = "error_code")
    private String errorCode;
    
    /**
     * Number of retry attempts made
     */
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;
    
    /**
     * Maximum number of retries allowed
     */
    @Column(name = "max_retries", nullable = false)
    private Integer maxRetries = 3;
    
    /**
     * Timestamp when the transaction was created
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    /**
     * Timestamp when the transaction was last updated
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * Timestamp when the transaction was processed
     */
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    /**
     * Expiration timestamp for time-sensitive payments
     */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    /**
     * Webhook URL for status notifications
     */
    @Column(name = "webhook_url")
    private String webhookUrl;
    
    /**
     * Callback URL for redirecting users after payment
     */
    @Column(name = "callback_url")
    private String callbackUrl;
    
    /**
     * Additional metadata as key-value pairs
     */
    @ElementCollection
    @CollectionTable(name = "payment_transaction_metadata", 
                    joinColumns = @JoinColumn(name = "transaction_id"))
    @MapKeyColumn(name = "metadata_key")
    @Column(name = "metadata_value")
    private Map<String, String> metadata = new HashMap<>();
    
    /**
     * Reference to the related installment (if applicable)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parcela_id")
    @JsonIgnore
    private Parcela parcela;
    
    /**
     * Reference to the related purchase (if applicable)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compra_id")
    @JsonIgnore
    private Compra compra;
    
    /**
     * Reference to the related sale (if applicable)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venda_id")
    @JsonIgnore
    private Venda venda;
    
    // Constructors
    public PaymentTransaction() {}
    
    public PaymentTransaction(String transactionId, String providerId, PaymentMethod paymentMethod, 
                            BigDecimal amount, String description) {
        this.transactionId = transactionId;
        this.providerId = providerId;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.description = description;
        this.status = PaymentStatus.PENDING;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
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
    
    public String getProviderId() {
        return providerId;
    }
    
    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }
    
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public PaymentStatus getStatus() {
        return status;
    }
    
    public void setStatus(PaymentStatus status) {
        this.status = status;
        if (status.isFinalStatus() && this.processedAt == null) {
            this.processedAt = LocalDateTime.now();
        }
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
    
    public String getCustomerInfo() {
        return customerInfo;
    }
    
    public void setCustomerInfo(String customerInfo) {
        this.customerInfo = customerInfo;
    }
    
    public String getProviderResponse() {
        return providerResponse;
    }
    
    public void setProviderResponse(String providerResponse) {
        this.providerResponse = providerResponse;
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
    
    public Integer getRetryCount() {
        return retryCount;
    }
    
    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }
    
    public Integer getMaxRetries() {
        return maxRetries;
    }
    
    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
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
    
    public String getWebhookUrl() {
        return webhookUrl;
    }
    
    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }
    
    public String getCallbackUrl() {
        return callbackUrl;
    }
    
    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }
    
    public Map<String, String> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
    
    public Parcela getParcela() {
        return parcela;
    }
    
    public void setParcela(Parcela parcela) {
        this.parcela = parcela;
    }
    
    public Compra getCompra() {
        return compra;
    }
    
    public void setCompra(Compra compra) {
        this.compra = compra;
    }
    
    public Venda getVenda() {
        return venda;
    }
    
    public void setVenda(Venda venda) {
        this.venda = venda;
    }
    
    // Utility methods
    
    /**
     * Check if the transaction can be retried
     * @return true if retry is possible
     */
    public boolean canRetry() {
        return retryCount < maxRetries && 
               (status == PaymentStatus.FAILED || status == PaymentStatus.PENDING);
    }
    
    /**
     * Increment retry count
     */
    public void incrementRetryCount() {
        this.retryCount++;
    }
    
    /**
     * Check if the transaction is expired
     * @return true if the transaction is expired
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
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
     * Get metadata value by key
     * @param key metadata key
     * @return metadata value or null if not found
     */
    public String getMetadata(String key) {
        return this.metadata.get(key);
    }
    
    @Override
    public String toString() {
        return "PaymentTransaction{" +
                "id=" + id +
                ", transactionId='" + transactionId + '\'' +
                ", providerId='" + providerId + '\'' +
                ", paymentMethod=" + paymentMethod +
                ", status=" + status +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}

