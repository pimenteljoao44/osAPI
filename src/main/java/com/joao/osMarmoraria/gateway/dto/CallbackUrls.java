package com.joao.osMarmoraria.gateway.dto;

/**
 * DTO para URLs de callback
 */
public class CallbackUrls {

    private String successUrl;
    private String failureUrl;
    private String cancelUrl;
    private String pendingUrl;
    private String webhookUrl;
    private String returnUrl;

    // Construtores
    public CallbackUrls() {}

    public CallbackUrls(String successUrl, String failureUrl) {
        this.successUrl = successUrl;
        this.failureUrl = failureUrl;
    }

    public CallbackUrls(String successUrl, String failureUrl, String cancelUrl) {
        this.successUrl = successUrl;
        this.failureUrl = failureUrl;
        this.cancelUrl = cancelUrl;
    }

    // Getters e Setters
    public String getSuccessUrl() {
        return successUrl;
    }

    public void setSuccessUrl(String successUrl) {
        this.successUrl = successUrl;
    }

    public String getFailureUrl() {
        return failureUrl;
    }

    public void setFailureUrl(String failureUrl) {
        this.failureUrl = failureUrl;
    }

    public String getCancelUrl() {
        return cancelUrl;
    }

    public void setCancelUrl(String cancelUrl) {
        this.cancelUrl = cancelUrl;
    }

    public String getPendingUrl() {
        return pendingUrl;
    }

    public void setPendingUrl(String pendingUrl) {
        this.pendingUrl = pendingUrl;
    }

    public String getWebhookUrl() {
        return webhookUrl;
    }

    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    // Métodos utilitários
    public boolean hasSuccessUrl() {
        return successUrl != null && !successUrl.trim().isEmpty();
    }

    public boolean hasFailureUrl() {
        return failureUrl != null && !failureUrl.trim().isEmpty();
    }

    public boolean hasCancelUrl() {
        return cancelUrl != null && !cancelUrl.trim().isEmpty();
    }

    public boolean hasWebhookUrl() {
        return webhookUrl != null && !webhookUrl.trim().isEmpty();
    }

    public boolean isComplete() {
        return hasSuccessUrl() && hasFailureUrl();
    }

    // Factory methods para configurações comuns
    public static CallbackUrls basic(String baseUrl) {
        return new CallbackUrls(
                baseUrl + "/payment/success",
                baseUrl + "/payment/failure",
                baseUrl + "/payment/cancel"
        );
    }

    public static CallbackUrls withWebhook(String baseUrl, String webhookUrl) {
        CallbackUrls urls = basic(baseUrl);
        urls.setWebhookUrl(webhookUrl);
        return urls;
    }
}

