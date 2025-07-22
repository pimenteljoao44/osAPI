package com.joao.osMarmoraria.gateway;

import com.joao.osMarmoraria.domain.enums.PaymentMethod;
import com.joao.osMarmoraria.gateway.dto.*;

import java.util.List;

/**
 * Core interface for payment gateway implementations.
 * This interface defines the contract that all payment providers must implement
 * to integrate with the payment gateway abstraction layer.
 * 
 * The interface follows the Strategy Pattern, allowing different payment providers
 * to be used interchangeably while maintaining a consistent API.
 */
public interface PaymentGateway {
    
    /**
     * Process a single payment transaction
     * 
     * @param request Payment request containing all necessary information
     * @return PaymentResult with transaction status and details
     * @throws PaymentGatewayException if payment processing fails
     */
    PaymentResult processPayment(PaymentRequest request) throws PaymentGatewayException;
    
    /**
     * Process an installment payment transaction
     * 
     * @param request Installment payment request with installment details
     * @return PaymentResult with transaction status and details
     * @throws PaymentGatewayException if payment processing fails
     */
    PaymentResult processInstallmentPayment(InstallmentPaymentRequest request) throws PaymentGatewayException;
    
    /**
     * Query the current status of a payment transaction
     * 
     * @param transactionId Transaction identifier to query
     * @return PaymentStatus with current transaction status
     * @throws PaymentGatewayException if status query fails
     */
    PaymentStatusResponse queryPaymentStatus(String transactionId) throws PaymentGatewayException;
    
    /**
     * Process a refund for a completed payment
     * 
     * @param request Refund request with refund details
     * @return RefundResult with refund status and details
     * @throws PaymentGatewayException if refund processing fails
     */
    RefundResult processRefund(RefundRequest request) throws PaymentGatewayException;
    
    /**
     * Cancel a pending payment transaction
     * 
     * @param transactionId Transaction identifier to cancel
     * @return CancelResult with cancellation status
     * @throws PaymentGatewayException if cancellation fails
     */
    CancelResult cancelPayment(String transactionId) throws PaymentGatewayException;
    
    /**
     * Process a webhook notification from the payment provider
     * 
     * @param request Webhook request with notification data
     * @return WebhookResult indicating processing status
     * @throws PaymentGatewayException if webhook processing fails
     */
    WebhookResult processWebhook(WebhookRequest request) throws PaymentGatewayException;
    
    /**
     * Get the list of payment methods supported by this provider
     * 
     * @return List of supported PaymentMethod enums
     */
    List<PaymentMethod> getSupportedPaymentMethods();
    
    /**
     * Get the capabilities of this payment provider
     * 
     * @return ProviderCapabilities describing what this provider can do
     */
    ProviderCapabilities getCapabilities();
    
    /**
     * Get the current configuration of this payment provider
     * 
     * @return ProviderConfiguration with current settings
     */
    ProviderConfiguration getConfiguration();
    
    /**
     * Check if the payment provider is currently available and healthy
     * 
     * @return true if the provider is available for processing payments
     */
    boolean isAvailable();
    
    /**
     * Perform a health check on the payment provider
     * 
     * @return HealthCheckResult with provider health status
     */
    HealthCheckResult performHealthCheck();
    
    /**
     * Get the unique identifier for this payment provider
     * 
     * @return Provider identifier string
     */
    String getProviderId();
    
    /**
     * Get the human-readable name of this payment provider
     * 
     * @return Provider name string
     */
    String getProviderName();
    
    /**
     * Validate a payment request before processing
     * 
     * @param request Payment request to validate
     * @return ValidationResult with validation status and any errors
     */
    ValidationResult validatePaymentRequest(PaymentRequest request);
    
    /**
     * Calculate the processing fee for a given amount and payment method
     * 
     * @param amount Transaction amount
     * @param paymentMethod Payment method to be used
     * @return Calculated fee amount
     */
    java.math.BigDecimal calculateFee(java.math.BigDecimal amount, PaymentMethod paymentMethod);
    
    /**
     * Get the estimated processing time for a payment method
     * 
     * @param paymentMethod Payment method to check
     * @return Estimated processing time in minutes
     */
    int getEstimatedProcessingTime(PaymentMethod paymentMethod);
    
    /**
     * Check if a specific amount is supported by this provider
     * 
     * @param amount Amount to check
     * @param paymentMethod Payment method to be used
     * @return true if the amount is supported
     */
    boolean supportsAmount(java.math.BigDecimal amount, PaymentMethod paymentMethod);
    
    /**
     * Generate a payment link or QR code for the given payment request
     * This is useful for PIX payments, boleto generation, etc.
     * 
     * @param request Payment request for which to generate the link
     * @return PaymentLinkResult with the generated link or QR code
     * @throws PaymentGatewayException if link generation fails
     */
    PaymentLinkResult generatePaymentLink(PaymentRequest request) throws PaymentGatewayException;
    
    /**
     * Get transaction history for a specific period or criteria
     * 
     * @param request Transaction history request with search criteria
     * @return TransactionHistoryResult with matching transactions
     * @throws PaymentGatewayException if history retrieval fails
     */
    TransactionHistoryResult getTransactionHistory(TransactionHistoryRequest request) throws PaymentGatewayException;
}

