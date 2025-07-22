package com.joao.osMarmoraria.gateway.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para opções de pagamento
 */
public class PaymentOptions {

    private boolean allowInstallments;
    private Integer maxInstallments;
    private BigDecimal minInstallmentAmount;
    private boolean allowPartialPayments;
    private boolean requireAuthentication;
    private boolean captureImmediately;
    private LocalDateTime expiresAt;
    private String preferredProvider;
    private boolean enableAntifraud;
    private String currency;
    private String locale;

    // Construtores
    public PaymentOptions() {
        this.currency = "BRL";
        this.locale = "pt-BR";
        this.captureImmediately = true;
        this.requireAuthentication = false;
        this.enableAntifraud = true;
    }

    // Getters e Setters
    public boolean isAllowInstallments() {
        return allowInstallments;
    }

    public void setAllowInstallments(boolean allowInstallments) {
        this.allowInstallments = allowInstallments;
    }

    public Integer getMaxInstallments() {
        return maxInstallments;
    }

    public void setMaxInstallments(Integer maxInstallments) {
        this.maxInstallments = maxInstallments;
    }

    public BigDecimal getMinInstallmentAmount() {
        return minInstallmentAmount;
    }

    public void setMinInstallmentAmount(BigDecimal minInstallmentAmount) {
        this.minInstallmentAmount = minInstallmentAmount;
    }

    public boolean isAllowPartialPayments() {
        return allowPartialPayments;
    }

    public void setAllowPartialPayments(boolean allowPartialPayments) {
        this.allowPartialPayments = allowPartialPayments;
    }

    public boolean isRequireAuthentication() {
        return requireAuthentication;
    }

    public void setRequireAuthentication(boolean requireAuthentication) {
        this.requireAuthentication = requireAuthentication;
    }

    public boolean isCaptureImmediately() {
        return captureImmediately;
    }

    public void setCaptureImmediately(boolean captureImmediately) {
        this.captureImmediately = captureImmediately;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getPreferredProvider() {
        return preferredProvider;
    }

    public void setPreferredProvider(String preferredProvider) {
        this.preferredProvider = preferredProvider;
    }

    public boolean isEnableAntifraud() {
        return enableAntifraud;
    }

    public void setEnableAntifraud(boolean enableAntifraud) {
        this.enableAntifraud = enableAntifraud;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    // Métodos utilitários
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean hasPreferredProvider() {
        return preferredProvider != null && !preferredProvider.trim().isEmpty();
    }

    // Factory methods para configurações comuns
    public static PaymentOptions defaultOptions() {
        return new PaymentOptions();
    }

    public static PaymentOptions withInstallments(int maxInstallments) {
        PaymentOptions options = new PaymentOptions();
        options.setAllowInstallments(true);
        options.setMaxInstallments(maxInstallments);
        return options;
    }

    public static PaymentOptions withInstallments(int maxInstallments, BigDecimal minAmount) {
        PaymentOptions options = withInstallments(maxInstallments);
        options.setMinInstallmentAmount(minAmount);
        return options;
    }

    public static PaymentOptions preAuthOnly() {
        PaymentOptions options = new PaymentOptions();
        options.setCaptureImmediately(false);
        return options;
    }
}


