package com.joao.osMarmoraria.gateway.dto;

import com.joao.osMarmoraria.domain.enums.PaymentMethod;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * DTO que representa as capacidades de um provedor de pagamento
 */
public class ProviderCapabilities {

    private String providerId;
    private String providerName;
    private List<PaymentMethod> supportedPaymentMethods;
    private Map<PaymentMethod, PaymentMethodCapability> methodCapabilities;
    private boolean supportsRefunds;
    private boolean supportsCancellation;
    private boolean supportsPartialRefunds;
    private boolean supportsInstallments;
    private boolean supportsRecurringPayments;
    private boolean supportsWebhooks;
    private BigDecimal minTransactionAmount;
    private BigDecimal maxTransactionAmount;
    private List<String> supportedCurrencies;
    private Map<String, Object> additionalCapabilities;

    // Construtores
    public ProviderCapabilities() {}

    public ProviderCapabilities(String providerId, String providerName) {
        this.providerId = providerId;
        this.providerName = providerName;
    }

    // Getters e Setters
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

    public List<PaymentMethod> getSupportedPaymentMethods() {
        return supportedPaymentMethods;
    }

    public void setSupportedPaymentMethods(List<PaymentMethod> supportedPaymentMethods) {
        this.supportedPaymentMethods = supportedPaymentMethods;
    }

    public Map<PaymentMethod, PaymentMethodCapability> getMethodCapabilities() {
        return methodCapabilities;
    }

    public void setMethodCapabilities(Map<PaymentMethod, PaymentMethodCapability> methodCapabilities) {
        this.methodCapabilities = methodCapabilities;
    }

    public boolean isSupportsRefunds() {
        return supportsRefunds;
    }

    public void setSupportsRefunds(boolean supportsRefunds) {
        this.supportsRefunds = supportsRefunds;
    }

    public boolean isSupportsCancellation() {
        return supportsCancellation;
    }

    public void setSupportsCancellation(boolean supportsCancellation) {
        this.supportsCancellation = supportsCancellation;
    }

    public boolean isSupportsPartialRefunds() {
        return supportsPartialRefunds;
    }

    public void setSupportsPartialRefunds(boolean supportsPartialRefunds) {
        this.supportsPartialRefunds = supportsPartialRefunds;
    }

    public boolean isSupportsInstallments() {
        return supportsInstallments;
    }

    public void setSupportsInstallments(boolean supportsInstallments) {
        this.supportsInstallments = supportsInstallments;
    }

    public boolean isSupportsRecurringPayments() {
        return supportsRecurringPayments;
    }

    public void setSupportsRecurringPayments(boolean supportsRecurringPayments) {
        this.supportsRecurringPayments = supportsRecurringPayments;
    }

    public boolean isSupportsWebhooks() {
        return supportsWebhooks;
    }

    public void setSupportsWebhooks(boolean supportsWebhooks) {
        this.supportsWebhooks = supportsWebhooks;
    }

    public BigDecimal getMinTransactionAmount() {
        return minTransactionAmount;
    }

    public void setMinTransactionAmount(BigDecimal minTransactionAmount) {
        this.minTransactionAmount = minTransactionAmount;
    }

    public BigDecimal getMaxTransactionAmount() {
        return maxTransactionAmount;
    }

    public void setMaxTransactionAmount(BigDecimal maxTransactionAmount) {
        this.maxTransactionAmount = maxTransactionAmount;
    }

    public List<String> getSupportedCurrencies() {
        return supportedCurrencies;
    }

    public void setSupportedCurrencies(List<String> supportedCurrencies) {
        this.supportedCurrencies = supportedCurrencies;
    }

    public Map<String, Object> getAdditionalCapabilities() {
        return additionalCapabilities;
    }

    public void setAdditionalCapabilities(Map<String, Object> additionalCapabilities) {
        this.additionalCapabilities = additionalCapabilities;
    }

    // Classe interna para capacidades específicas de método de pagamento
    public static class PaymentMethodCapability {
        private PaymentMethod method;
        private BigDecimal minAmount;
        private BigDecimal maxAmount;
        private Integer maxInstallments;
        private boolean supportsCapture;
        private boolean supportsPreAuth;
        private Map<String, Object> additionalProperties;

        public PaymentMethodCapability() {}

        public PaymentMethodCapability(PaymentMethod method) {
            this.method = method;
        }

        // Getters e Setters
        public PaymentMethod getMethod() {
            return method;
        }

        public void setMethod(PaymentMethod method) {
            this.method = method;
        }

        public BigDecimal getMinAmount() {
            return minAmount;
        }

        public void setMinAmount(BigDecimal minAmount) {
            this.minAmount = minAmount;
        }

        public BigDecimal getMaxAmount() {
            return maxAmount;
        }

        public void setMaxAmount(BigDecimal maxAmount) {
            this.maxAmount = maxAmount;
        }

        public Integer getMaxInstallments() {
            return maxInstallments;
        }

        public void setMaxInstallments(Integer maxInstallments) {
            this.maxInstallments = maxInstallments;
        }

        public boolean isSupportsCapture() {
            return supportsCapture;
        }

        public void setSupportsCapture(boolean supportsCapture) {
            this.supportsCapture = supportsCapture;
        }

        public boolean isSupportsPreAuth() {
            return supportsPreAuth;
        }

        public void setSupportsPreAuth(boolean supportsPreAuth) {
            this.supportsPreAuth = supportsPreAuth;
        }

        public Map<String, Object> getAdditionalProperties() {
            return additionalProperties;
        }

        public void setAdditionalProperties(Map<String, Object> additionalProperties) {
            this.additionalProperties = additionalProperties;
        }
    }
}

