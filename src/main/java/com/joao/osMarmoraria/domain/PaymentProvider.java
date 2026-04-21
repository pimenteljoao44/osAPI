package com.joao.osMarmoraria.domain;

import com.joao.osMarmoraria.domain.enums.PaymentMethod;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Entity representing a payment provider configuration in the payment gateway abstraction layer.
 * This entity stores provider-specific settings, credentials, and capabilities.
 */
@Entity
@Table(name = "payment_providers")
public class PaymentProvider {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Unique provider identifier
     */
    @Column(name = "provider_id", unique = true, nullable = false)
    private String providerId;
    
    /**
     * Human-readable provider name
     */
    @Column(name = "provider_name", nullable = false)
    private String providerName;
    
    /**
     * Provider description
     */
    @Column(name = "description", length = 500)
    private String description;
    
    /**
     * Whether the provider is currently enabled
     */
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;
    
    /**
     * Provider priority for selection (lower number = higher priority)
     */
    @Column(name = "priority", nullable = false)
    private Integer priority = 100;
    
    /**
     * Base API URL for the provider
     */
    @Column(name = "api_url")
    private String apiUrl;
    
    /**
     * API version being used
     */
    @Column(name = "api_version")
    private String apiVersion;
    
    /**
     * Environment (sandbox, production, etc.)
     */
    @Column(name = "environment", nullable = false)
    private String environment = "production";
    
    /**
     * Provider-specific configuration settings
     */
    @ElementCollection
    @CollectionTable(name = "payment_provider_settings", 
                    joinColumns = @JoinColumn(name = "provider_id"))
    @MapKeyColumn(name = "setting_key")
    @Column(name = "setting_value")
    private Map<String, String> settings = new HashMap<>();
    
    /**
     * Encrypted credentials for the provider
     */
    @ElementCollection
    @CollectionTable(name = "payment_provider_credentials", 
                    joinColumns = @JoinColumn(name = "provider_id"))
    @MapKeyColumn(name = "credential_key")
    @Column(name = "credential_value", columnDefinition = "TEXT")
    private Map<String, String> credentials = new HashMap<>();
    
    /**
     * Payment methods supported by this provider
     */
    @ElementCollection(targetClass = PaymentMethod.class)
    @CollectionTable(name = "payment_provider_methods", 
                    joinColumns = @JoinColumn(name = "provider_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private Set<PaymentMethod> supportedPaymentMethods = new HashSet<>();
    
    /**
     * Maximum transaction amount supported
     */
    @Column(name = "max_amount", precision = 19, scale = 2)
    private java.math.BigDecimal maxAmount;
    
    /**
     * Minimum transaction amount supported
     */
    @Column(name = "min_amount", precision = 19, scale = 2)
    private java.math.BigDecimal minAmount;
    
    /**
     * Default timeout for API calls in seconds
     */
    @Column(name = "timeout_seconds", nullable = false)
    private Integer timeoutSeconds = 30;
    
    /**
     * Maximum number of retry attempts
     */
    @Column(name = "max_retries", nullable = false)
    private Integer maxRetries = 3;
    
    /**
     * Whether the provider supports webhooks
     */
    @Column(name = "supports_webhooks", nullable = false)
    private Boolean supportsWebhooks = false;
    
    /**
     * Whether the provider supports refunds
     */
    @Column(name = "supports_refunds", nullable = false)
    private Boolean supportsRefunds = true;
    
    /**
     * Whether the provider supports installments
     */
    @Column(name = "supports_installments", nullable = false)
    private Boolean supportsInstallments = false;
    
    /**
     * Processing fee percentage (if applicable)
     */
    @Column(name = "fee_percentage", precision = 5, scale = 4)
    private java.math.BigDecimal feePercentage;
    
    /**
     * Fixed processing fee (if applicable)
     */
    @Column(name = "fixed_fee", precision = 19, scale = 2)
    private java.math.BigDecimal fixedFee;
    
    /**
     * Timestamp when the provider was created
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    /**
     * Timestamp when the provider was last updated
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * Timestamp when the provider was last tested
     */
    @Column(name = "last_tested_at")
    private LocalDateTime lastTestedAt;
    
    /**
     * Result of the last health check
     */
    @Column(name = "health_status")
    private String healthStatus;
    
    // Constructors
    public PaymentProvider() {}
    
    public PaymentProvider(String providerId, String providerName) {
        this.providerId = providerId;
        this.providerName = providerName;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
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
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Boolean getEnabled() {
        return enabled;
    }
    
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
    
    public Integer getPriority() {
        return priority;
    }
    
    public void setPriority(Integer priority) {
        this.priority = priority;
    }
    
    public String getApiUrl() {
        return apiUrl;
    }
    
    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }
    
    public String getApiVersion() {
        return apiVersion;
    }
    
    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }
    
    public String getEnvironment() {
        return environment;
    }
    
    public void setEnvironment(String environment) {
        this.environment = environment;
    }
    
    public Map<String, String> getSettings() {
        return settings;
    }
    
    public void setSettings(Map<String, String> settings) {
        this.settings = settings;
    }
    
    public Map<String, String> getCredentials() {
        return credentials;
    }
    
    public void setCredentials(Map<String, String> credentials) {
        this.credentials = credentials;
    }
    
    public Set<PaymentMethod> getSupportedPaymentMethods() {
        return supportedPaymentMethods;
    }
    
    public void setSupportedPaymentMethods(Set<PaymentMethod> supportedPaymentMethods) {
        this.supportedPaymentMethods = supportedPaymentMethods;
    }
    
    public java.math.BigDecimal getMaxAmount() {
        return maxAmount;
    }
    
    public void setMaxAmount(java.math.BigDecimal maxAmount) {
        this.maxAmount = maxAmount;
    }
    
    public java.math.BigDecimal getMinAmount() {
        return minAmount;
    }
    
    public void setMinAmount(java.math.BigDecimal minAmount) {
        this.minAmount = minAmount;
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
    
    public Boolean getSupportsWebhooks() {
        return supportsWebhooks;
    }
    
    public void setSupportsWebhooks(Boolean supportsWebhooks) {
        this.supportsWebhooks = supportsWebhooks;
    }
    
    public Boolean getSupportsRefunds() {
        return supportsRefunds;
    }
    
    public void setSupportsRefunds(Boolean supportsRefunds) {
        this.supportsRefunds = supportsRefunds;
    }
    
    public Boolean getSupportsInstallments() {
        return supportsInstallments;
    }
    
    public void setSupportsInstallments(Boolean supportsInstallments) {
        this.supportsInstallments = supportsInstallments;
    }
    
    public java.math.BigDecimal getFeePercentage() {
        return feePercentage;
    }
    
    public void setFeePercentage(java.math.BigDecimal feePercentage) {
        this.feePercentage = feePercentage;
    }
    
    public java.math.BigDecimal getFixedFee() {
        return fixedFee;
    }
    
    public void setFixedFee(java.math.BigDecimal fixedFee) {
        this.fixedFee = fixedFee;
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
    
    public LocalDateTime getLastTestedAt() {
        return lastTestedAt;
    }
    
    public void setLastTestedAt(LocalDateTime lastTestedAt) {
        this.lastTestedAt = lastTestedAt;
    }
    
    public String getHealthStatus() {
        return healthStatus;
    }
    
    public void setHealthStatus(String healthStatus) {
        this.healthStatus = healthStatus;
    }
    
    // Utility methods
    
    /**
     * Add a setting
     * @param key setting key
     * @param value setting value
     */
    public void addSetting(String key, String value) {
        this.settings.put(key, value);
    }
    
    /**
     * Get a setting value
     * @param key setting key
     * @return setting value or null if not found
     */
    public String getSetting(String key) {
        return this.settings.get(key);
    }
    
    /**
     * Add a credential
     * @param key credential key
     * @param value credential value (will be encrypted)
     */
    public void addCredential(String key, String value) {
        this.credentials.put(key, value);
    }
    
    /**
     * Get a credential value
     * @param key credential key
     * @return credential value (decrypted) or null if not found
     */
    public String getCredential(String key) {
        return this.credentials.get(key);
    }
    
    /**
     * Add a supported payment method
     * @param paymentMethod payment method to add
     */
    public void addSupportedPaymentMethod(PaymentMethod paymentMethod) {
        this.supportedPaymentMethods.add(paymentMethod);
    }
    
    /**
     * Check if a payment method is supported
     * @param paymentMethod payment method to check
     * @return true if supported
     */
    public boolean supportsPaymentMethod(PaymentMethod paymentMethod) {
        return this.supportedPaymentMethods.contains(paymentMethod);
    }
    
    /**
     * Check if an amount is within the supported range
     * @param amount amount to check
     * @return true if amount is supported
     */
    public boolean supportsAmount(java.math.BigDecimal amount) {
        if (minAmount != null && amount.compareTo(minAmount) < 0) {
            return false;
        }
        if (maxAmount != null && amount.compareTo(maxAmount) > 0) {
            return false;
        }
        return true;
    }
    
    /**
     * Calculate processing fee for an amount
     * @param amount transaction amount
     * @return calculated fee
     */
    public java.math.BigDecimal calculateFee(java.math.BigDecimal amount) {
        java.math.BigDecimal fee = java.math.BigDecimal.ZERO;
        
        if (feePercentage != null) {
            fee = fee.add(amount.multiply(feePercentage));
        }
        
        if (fixedFee != null) {
            fee = fee.add(fixedFee);
        }
        
        return fee;
    }
    
    /**
     * Check if the provider is healthy and available
     * @return true if provider is available
     */
    public boolean isAvailable() {
        return enabled && "HEALTHY".equals(healthStatus);
    }
    
    @Override
    public String toString() {
        return "PaymentProvider{" +
                "id=" + id +
                ", providerId='" + providerId + '\'' +
                ", providerName='" + providerName + '\'' +
                ", enabled=" + enabled +
                ", environment='" + environment + '\'' +
                ", supportedPaymentMethods=" + supportedPaymentMethods +
                '}';
    }
}

