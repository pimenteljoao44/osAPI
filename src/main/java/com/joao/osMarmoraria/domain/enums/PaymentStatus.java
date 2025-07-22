package com.joao.osMarmoraria.domain.enums;

/**
 * Enum representing the various states of a payment transaction
 * throughout its lifecycle in the payment gateway abstraction layer.
 */
public enum PaymentStatus {
    
    /**
     * Payment has been initiated but not yet processed
     */
    PENDING("Pendente"),
    
    /**
     * Payment is currently being processed by the provider
     */
    PROCESSING("Processando"),
    
    /**
     * Payment has been successfully completed
     */
    COMPLETED("Concluído"),
    
    /**
     * Payment has failed due to various reasons
     */
    FAILED("Falhou"),
    
    /**
     * Payment has been cancelled before completion
     */
    CANCELLED("Cancelado"),
    
    /**
     * Payment has been refunded (full or partial)
     */
    REFUNDED("Reembolsado"),
    
    /**
     * Payment is expired (for time-sensitive payments like PIX)
     */
    EXPIRED("Expirado"),
    
    /**
     * Payment requires additional action from the user
     */
    REQUIRES_ACTION("Requer Ação"),
    
    /**
     * Payment is under review (fraud detection, compliance)
     */
    UNDER_REVIEW("Em Análise");
    
    private final String descricao;
    
    PaymentStatus(String descricao) {
        this.descricao = descricao;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    /**
     * Check if the payment status represents a final state
     * @return true if the status is final (no further changes expected)
     */
    public boolean isFinalStatus() {
        return this == COMPLETED || this == FAILED || 
               this == CANCELLED || this == REFUNDED || this == EXPIRED;
    }
    
    /**
     * Check if the payment status represents a successful outcome
     * @return true if the payment was successful
     */
    public boolean isSuccessful() {
        return this == COMPLETED;
    }
    
    /**
     * Check if the payment can be cancelled
     * @return true if the payment can be cancelled
     */
    public boolean isCancellable() {
        return this == PENDING || this == PROCESSING || this == REQUIRES_ACTION;
    }
    
    /**
     * Check if the payment can be refunded
     * @return true if the payment can be refunded
     */
    public boolean isRefundable() {
        return this == COMPLETED;
    }
}

