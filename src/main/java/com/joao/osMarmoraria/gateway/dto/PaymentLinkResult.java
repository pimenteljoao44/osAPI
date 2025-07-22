package com.joao.osMarmoraria.gateway.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO para resultado de criação de link de pagamento
 */
public class PaymentLinkResult {

    private boolean success;
    private String paymentLinkId;
    private String paymentUrl;
    private String qrCode;
    private String qrCodeImage; // Base64 encoded image
    private LocalDateTime expiresAt;
    private String status;
    private String errorMessage;
    private String errorCode;
    private Map<String, Object> metadata;

    // Construtores
    public PaymentLinkResult() {}

    public PaymentLinkResult(boolean success) {
        this.success = success;
    }

    public PaymentLinkResult(boolean success, String paymentUrl) {
        this.success = success;
        this.paymentUrl = paymentUrl;
    }

    // Getters e Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getPaymentLinkId() {
        return paymentLinkId;
    }

    public void setPaymentLinkId(String paymentLinkId) {
        this.paymentLinkId = paymentLinkId;
    }

    public String getPaymentUrl() {
        return paymentUrl;
    }

    public void setPaymentUrl(String paymentUrl) {
        this.paymentUrl = paymentUrl;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public String getQrCodeImage() {
        return qrCodeImage;
    }

    public void setQrCodeImage(String qrCodeImage) {
        this.qrCodeImage = qrCodeImage;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    // Métodos utilitários
    public void addMetadata(String key, Object value) {
        if (metadata == null) {
            metadata = new java.util.HashMap<>();
        }
        metadata.put(key, value);
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean hasQrCode() {
        return qrCode != null && !qrCode.trim().isEmpty();
    }

    public boolean hasQrCodeImage() {
        return qrCodeImage != null && !qrCodeImage.trim().isEmpty();
    }

    // Factory methods para criação rápida
    public static PaymentLinkResult success(String paymentUrl) {
        PaymentLinkResult result = new PaymentLinkResult(true, paymentUrl);
        result.setStatus("CREATED");
        return result;
    }

    public static PaymentLinkResult success(String paymentUrl, String qrCode) {
        PaymentLinkResult result = success(paymentUrl);
        result.setQrCode(qrCode);
        return result;
    }

    public static PaymentLinkResult failure(String errorMessage) {
        PaymentLinkResult result = new PaymentLinkResult(false);
        result.setErrorMessage(errorMessage);
        result.setStatus("FAILED");
        return result;
    }

    public static PaymentLinkResult failure(String errorCode, String errorMessage) {
        PaymentLinkResult result = failure(errorMessage);
        result.setErrorCode(errorCode);
        return result;
    }
}