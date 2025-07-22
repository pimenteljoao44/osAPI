package com.joao.osMarmoraria.services;

import com.joao.osMarmoraria.domain.PaymentProvider;
import com.joao.osMarmoraria.domain.PaymentTransaction;
import com.joao.osMarmoraria.domain.Parcela;
import com.joao.osMarmoraria.domain.enums.PaymentMethod;
import com.joao.osMarmoraria.domain.enums.PaymentStatus;
import com.joao.osMarmoraria.dtos.ParcelaDTO;
import com.joao.osMarmoraria.gateway.PaymentGateway;
import com.joao.osMarmoraria.gateway.PaymentGatewayException;
import com.joao.osMarmoraria.gateway.dto.*;
import com.joao.osMarmoraria.repository.PaymentProviderRepository;
import com.joao.osMarmoraria.repository.PaymentTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for orchestrating payment processing through the payment gateway abstraction layer.
 * This service implements the Strategy Pattern to select appropriate payment providers
 * and coordinates payment processing workflows.
 */
@Service
@Transactional
public class PaymentGatewayService {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentGatewayService.class);
    
    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;
    
    @Autowired
    private PaymentProviderRepository paymentProviderRepository;
    
    @Autowired
    private ParcelaService parcelaService;
    
    /**
     * Map of registered payment gateway implementations
     */
    private final Map<String, PaymentGateway> paymentGateways = new HashMap<>();
    
    /**
     * Register a payment gateway implementation
     * @param providerId provider identifier
     * @param gateway payment gateway implementation
     */
    public void registerPaymentGateway(String providerId, PaymentGateway gateway) {
        paymentGateways.put(providerId, gateway);
        logger.info("Registered payment gateway: {} - {}", providerId, gateway.getProviderName());
    }
    
    /**
     * Process a payment using the most appropriate provider
     * @param request payment request
     * @return payment result
     * @throws PaymentGatewayException if payment processing fails
     */
    public PaymentResult processPayment(PaymentRequest request) throws PaymentGatewayException {
        logger.info("Processing payment request: {}", request.getRequestId());
        
        // Validate the payment request
        validatePaymentRequest(request);
        
        // Select the best provider for this payment
        PaymentProvider provider = selectPaymentProvider(request.getPaymentMethod(), request.getAmount());
        PaymentGateway gateway = getPaymentGateway(provider.getProviderId());
        
        // Create transaction record
        PaymentTransaction transaction = createPaymentTransaction(request, provider);
        
        try {
            // Process the payment
            PaymentResult result = gateway.processPayment(request);
            
            // Update transaction with result
            updateTransactionFromResult(transaction, result);
            
            logger.info("Payment processed successfully: {} - Status: {}", 
                       transaction.getTransactionId(), result.getStatus());
            
            return result;
            
        } catch (PaymentGatewayException e) {
            // Handle payment failure
            handlePaymentFailure(transaction, e);
            throw e;
        }
    }
    
    /**
     * Process an installment payment
     * @param parcelaId installment ID
     * @return payment result
     * @throws PaymentGatewayException if payment processing fails
     */
    public PaymentResult processInstallmentPayment(Integer parcelaId) throws PaymentGatewayException {
        logger.info("Processing installment payment for parcela: {}", parcelaId);
        
        ParcelaDTO parcela = parcelaService.buscarPorId(parcelaId);
        if (parcela == null) {
            throw PaymentGatewayException.invalidRequest("Installment not found: " + parcelaId);
        }
        
        // Create payment request from installment
        PaymentRequest request = createPaymentRequestFromParcela(parcela);
        
        // Process the payment
        PaymentResult result = processPayment(request);
        
        // Update installment status if payment was successful
        if (result.isPaymentSuccessful()) {
            parcelaService.marcarParcelaComoPaga(parcelaId, LocalDate.now());
        }
        
        return result;
    }
    
    /**
     * Query payment status
     * @param transactionId transaction ID
     * @return payment status response
     * @throws PaymentGatewayException if status query fails
     */
    public PaymentStatusResponse queryPaymentStatus(String transactionId) throws PaymentGatewayException {
        PaymentTransaction transaction = paymentTransactionRepository.findByTransactionId(transactionId);
        if (transaction == null) {
            throw PaymentGatewayException.invalidRequest("Transaction not found: " + transactionId);
        }
        
        PaymentGateway gateway = getPaymentGateway(transaction.getProviderId());
        return gateway.queryPaymentStatus(transactionId);
    }
    
    /**
     * Process a refund
     * @param transactionId original transaction ID
     * @param amount refund amount (null for full refund)
     * @param reason refund reason
     * @return refund result
     * @throws PaymentGatewayException if refund processing fails
     */
    public RefundResult processRefund(String transactionId, BigDecimal amount, String reason) 
            throws PaymentGatewayException {
        
        PaymentTransaction transaction = paymentTransactionRepository.findByTransactionId(transactionId);
        if (transaction == null) {
            throw PaymentGatewayException.invalidRequest("Transaction not found: " + transactionId);
        }
        
        if (!transaction.getStatus().isRefundable()) {
            throw PaymentGatewayException.invalidRequest("Transaction cannot be refunded: " + transactionId);
        }
        
        PaymentGateway gateway = getPaymentGateway(transaction.getProviderId());
        
        RefundRequest refundRequest = new RefundRequest();
        refundRequest.setTransactionId(transactionId);
        refundRequest.setAmount(amount != null ? amount : transaction.getProcessedAmount());
        refundRequest.setReason(reason);
        
        RefundResult result = gateway.processRefund(refundRequest);
        
        // Update transaction status
        if (result.isSuccess()) {
            transaction.setStatus(PaymentStatus.REFUNDED);
            paymentTransactionRepository.save(transaction);
        }
        
        return result;
    }
    
    /**
     * Cancel a pending payment
     * @param transactionId transaction ID
     * @return cancel result
     * @throws PaymentGatewayException if cancellation fails
     */
    public CancelResult cancelPayment(String transactionId) throws PaymentGatewayException {
        PaymentTransaction transaction = paymentTransactionRepository.findByTransactionId(transactionId);
        if (transaction == null) {
            throw PaymentGatewayException.invalidRequest("Transaction not found: " + transactionId);
        }
        
        if (!transaction.getStatus().isCancellable()) {
            throw PaymentGatewayException.invalidRequest("Transaction cannot be cancelled: " + transactionId);
        }
        
        PaymentGateway gateway = getPaymentGateway(transaction.getProviderId());
        CancelResult result = gateway.cancelPayment(transactionId);
        
        // Update transaction status
        if (result.isSuccess()) {
            transaction.setStatus(PaymentStatus.CANCELLED);
            paymentTransactionRepository.save(transaction);
        }
        
        return result;
    }
    
    /**
     * Get available payment methods for a given amount
     * @param amount payment amount
     * @return list of available payment methods
     */
    public List<PaymentMethod> getAvailablePaymentMethods(BigDecimal amount) {
        List<PaymentProvider> availableProviders = paymentProviderRepository.findByEnabledTrueOrderByPriority();
        
        return availableProviders.stream()
                .filter(provider -> provider.supportsAmount(amount))
                .flatMap(provider -> provider.getSupportedPaymentMethods().stream())
                .distinct()
                .collect(Collectors.toList());
    }
    
    /**
     * Get payment providers for a specific payment method
     * @param paymentMethod payment method
     * @return list of supporting providers
     */
    public List<PaymentProvider> getProvidersForPaymentMethod(PaymentMethod paymentMethod) {
        return paymentProviderRepository.findByEnabledTrueAndSupportedPaymentMethodsContaining(paymentMethod);
    }
    
    /**
     * Process webhook notification
     * @param providerId provider ID
     * @param webhookRequest webhook request
     * @return webhook result
     * @throws PaymentGatewayException if webhook processing fails
     */
    public WebhookResult processWebhook(String providerId, WebhookRequest webhookRequest) 
            throws PaymentGatewayException {
        
        PaymentGateway gateway = getPaymentGateway(providerId);
        WebhookResult result = gateway.processWebhook(webhookRequest);
        
        // Update transaction status based on webhook
        if (result.getTransactionId() != null) {
            updateTransactionFromWebhook(result);
        }
        
        return result;
    }
    
    // Private helper methods
    
    private void validatePaymentRequest(PaymentRequest request) throws PaymentGatewayException {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw PaymentGatewayException.invalidRequest("Invalid payment amount");
        }
        
        if (request.getPaymentMethod() == null) {
            throw PaymentGatewayException.invalidRequest("Payment method is required");
        }
        
        if (request.isExpired()) {
            throw PaymentGatewayException.invalidRequest("Payment request has expired");
        }
    }
    
    private PaymentProvider selectPaymentProvider(PaymentMethod paymentMethod, BigDecimal amount) 
            throws PaymentGatewayException {
        
        List<PaymentProvider> candidates = paymentProviderRepository
                .findByEnabledTrueAndSupportedPaymentMethodsContainingOrderByPriority(paymentMethod);
        
        for (PaymentProvider provider : candidates) {
            if (provider.supportsAmount(amount) && provider.isAvailable()) {
                PaymentGateway gateway = paymentGateways.get(provider.getProviderId());
                if (gateway != null && gateway.isAvailable()) {
                    return provider;
                }
            }
        }
        
        throw PaymentGatewayException.providerUnavailable(
                "No available provider for payment method: " + paymentMethod);
    }
    
    private PaymentGateway getPaymentGateway(String providerId) throws PaymentGatewayException {
        PaymentGateway gateway = paymentGateways.get(providerId);
        if (gateway == null) {
            throw PaymentGatewayException.configurationError("Payment gateway not found: " + providerId);
        }
        return gateway;
    }
    
    private PaymentTransaction createPaymentTransaction(PaymentRequest request, PaymentProvider provider) {
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setTransactionId(generateTransactionId());
        transaction.setProviderId(provider.getProviderId());
        transaction.setPaymentMethod(request.getPaymentMethod());
        transaction.setAmount(request.getAmount());
        transaction.setCurrency(request.getCurrency());
        transaction.setDescription(request.getDescription());
        transaction.setOrderReference(request.getOrderReference());
        transaction.setStatus(PaymentStatus.PENDING);
        transaction.setMaxRetries(provider.getMaxRetries());
        
        // Store customer info as JSON
        if (request.getCustomer() != null) {
            // In a real implementation, you would serialize customer info to JSON
            transaction.setCustomerInfo(request.getCustomer().toString());
        }
        
        // Store metadata
        if (request.getMetadata() != null) {
            transaction.setMetadata(request.getMetadata());
        }
        
        return paymentTransactionRepository.save(transaction);
    }
    
    private void updateTransactionFromResult(PaymentTransaction transaction, PaymentResult result) {
        transaction.setStatus(result.getStatus());
        transaction.setProviderTransactionId(result.getProviderTransactionId());
        transaction.setProcessedAmount(result.getProcessedAmount());
        transaction.setFees(result.getFees());
        
        if (result.hasErrors()) {
            PaymentError firstError = result.getErrors().get(0);
            transaction.setErrorMessage(firstError.getMessage());
            transaction.setErrorCode(firstError.getCode());
        }
        
        // Store provider response as JSON
        if (result.getProviderResponse() != null && !result.getProviderResponse().isEmpty()) {
            // In a real implementation, you would serialize provider response to JSON
            transaction.setProviderResponse(result.getProviderResponse().toString());
        }
        
        paymentTransactionRepository.save(transaction);
    }
    
    private void handlePaymentFailure(PaymentTransaction transaction, PaymentGatewayException e) {
        transaction.setStatus(PaymentStatus.FAILED);
        transaction.setErrorMessage(e.getMessage());
        transaction.setErrorCode(e.getErrorCode());
        transaction.incrementRetryCount();
        
        paymentTransactionRepository.save(transaction);
        
        logger.error("Payment failed for transaction: {} - Error: {}", 
                    transaction.getTransactionId(), e.getMessage());
    }
    
    private PaymentRequest createPaymentRequestFromParcela(ParcelaDTO parcela) {
        PaymentRequest request = new PaymentRequest();
        request.setRequestId(generateRequestId());
        request.setAmount(parcela.getValorParcela());
        request.setDescription("Pagamento de parcela #" + parcela.getNumeroParcela());
        request.setOrderReference(parcela.getId().toString());
        
        // Set payment method based on installment configuration
        // This would be determined by business logic
        request.setPaymentMethod(PaymentMethod.PIX); // Default to PIX for now
        
        return request;
    }
    
    private void updateTransactionFromWebhook(WebhookResult webhookResult) {
        PaymentTransaction transaction = paymentTransactionRepository
                .findByTransactionId(webhookResult.getTransactionId());
        
        if (transaction != null && webhookResult.getNewStatus() != null) {
            transaction.setStatus(webhookResult.getNewStatus());
            paymentTransactionRepository.save(transaction);
            
            logger.info("Updated transaction status from webhook: {} - New status: {}", 
                       transaction.getTransactionId(), webhookResult.getNewStatus());
        }
    }
    
    private String generateTransactionId() {
        return "TXN_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    private String generateRequestId() {
        return "REQ_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
}

