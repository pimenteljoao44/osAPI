package com.joao.osMarmoraria.gateway.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO para recibo de pagamento
 */
public class PaymentReceipt {

    private String receiptId;
    private String transactionId;
    private String providerTransactionId;
    private LocalDateTime paymentDate;
    private String paymentMethod;
    private BigDecimal amountPaid;
    private String currency;
    private String customerName;
    private String customerDocument;
    private String companyName;
    private String companyDocument;
    private String description;
    private String authenticationCode;
    private Map<String, String> additionalInfo;

    // Construtores
    public PaymentReceipt() {}

    public PaymentReceipt(String transactionId, LocalDateTime paymentDate, BigDecimal amountPaid) {
        this.transactionId = transactionId;
        this.paymentDate = paymentDate;
        this.amountPaid = amountPaid;
    }

    // Getters e Setters
    public String getReceiptId() {
        return receiptId;
    }

    public void setReceiptId(String receiptId) {
        this.receiptId = receiptId;
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

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(BigDecimal amountPaid) {
        this.amountPaid = amountPaid;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerDocument() {
        return customerDocument;
    }

    public void setCustomerDocument(String customerDocument) {
        this.customerDocument = customerDocument;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyDocument() {
        return companyDocument;
    }

    public void setCompanyDocument(String companyDocument) {
        this.companyDocument = companyDocument;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAuthenticationCode() {
        return authenticationCode;
    }

    public void setAuthenticationCode(String authenticationCode) {
        this.authenticationCode = authenticationCode;
    }

    public Map<String, String> getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(Map<String, String> additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    // Métodos utilitários
    public void addInfo(String key, String value) {
        if (additionalInfo == null) {
            additionalInfo = new java.util.HashMap<>();
        }
        additionalInfo.put(key, value);
    }
}

