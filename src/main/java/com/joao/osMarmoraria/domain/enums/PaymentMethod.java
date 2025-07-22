package com.joao.osMarmoraria.domain.enums;

/**
 * Enum representing the various payment methods supported
 * by the payment gateway abstraction layer.
 */
public enum PaymentMethod {
    
    /**
     * PIX - Brazilian instant payment system
     */
    PIX("PIX", "PIX - Pagamento Instantâneo", true, true),
    
    /**
     * Credit Card payments
     */
    CREDIT_CARD("CREDIT_CARD", "Cartão de Crédito", true, true),
    
    /**
     * Debit Card payments
     */
    DEBIT_CARD("DEBIT_CARD", "Cartão de Débito", true, false),
    
    /**
     * Boleto Bancário - Brazilian bank slip
     */
    BOLETO("BOLETO", "Boleto Bancário", false, true),
    
    /**
     * Bank Transfer (TED/DOC)
     */
    BANK_TRANSFER("BANK_TRANSFER", "Transferência Bancária", false, false),
    
    /**
     * Digital Wallets (PicPay, Mercado Pago, etc.)
     */
    DIGITAL_WALLET("DIGITAL_WALLET", "Carteira Digital", true, true),
    
    /**
     * Cash payments
     */
    CASH("CASH", "Dinheiro", true, false),
    
    /**
     * Check payments
     */
    CHECK("CHECK", "Cheque", false, false),
    
    /**
     * Cryptocurrency payments
     */
    CRYPTOCURRENCY("CRYPTOCURRENCY", "Criptomoeda", true, true),
    
    /**
     * Buy Now Pay Later services
     */
    BNPL("BNPL", "Compre Agora Pague Depois", false, true);
    
    private final String code;
    private final String description;
    private final boolean instantPayment;
    private final boolean supportsInstallments;
    
    PaymentMethod(String code, String description, boolean instantPayment, boolean supportsInstallments) {
        this.code = code;
        this.description = description;
        this.instantPayment = instantPayment;
        this.supportsInstallments = supportsInstallments;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isInstantPayment() {
        return instantPayment;
    }
    
    public boolean supportsInstallments() {
        return supportsInstallments;
    }
    
    /**
     * Get PaymentMethod by code
     * @param code the payment method code
     * @return PaymentMethod or null if not found
     */
    public static PaymentMethod fromCode(String code) {
        for (PaymentMethod method : values()) {
            if (method.getCode().equals(code)) {
                return method;
            }
        }
        return null;
    }
    
    /**
     * Check if the payment method requires online processing
     * @return true if online processing is required
     */
    public boolean requiresOnlineProcessing() {
        return this == PIX || this == CREDIT_CARD || this == DEBIT_CARD || 
               this == DIGITAL_WALLET || this == CRYPTOCURRENCY;
    }
    
    /**
     * Check if the payment method supports refunds
     * @return true if refunds are supported
     */
    public boolean supportsRefunds() {
        return this != CASH && this != CHECK;
    }
    
    /**
     * Get the typical processing time in minutes
     * @return processing time in minutes
     */
    public int getTypicalProcessingTimeMinutes() {
        switch (this) {
            case PIX:
            case DIGITAL_WALLET:
                return 1; // Near instant
            case CREDIT_CARD:
            case DEBIT_CARD:
                return 5; // Few minutes
            case BOLETO:
                return 2880; // 2 days
            case BANK_TRANSFER:
                return 1440; // 1 day
            case CRYPTOCURRENCY:
                return 60; // 1 hour
            case BNPL:
                return 30; // 30 minutes
            default:
                return 0; // Immediate for cash/check
        }
    }
}

