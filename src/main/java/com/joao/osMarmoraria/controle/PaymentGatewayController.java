package com.joao.osMarmoraria.controle;

import com.joao.osMarmoraria.domain.PaymentProvider;
import com.joao.osMarmoraria.domain.PaymentTransaction;
import com.joao.osMarmoraria.domain.enums.PaymentMethod;
import com.joao.osMarmoraria.domain.enums.PaymentStatus;
import com.joao.osMarmoraria.gateway.PaymentGatewayException;
import com.joao.osMarmoraria.gateway.dto.*;
import com.joao.osMarmoraria.services.PaymentGatewayService;
import com.joao.osMarmoraria.repository.PaymentTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for payment gateway operations.
 * Provides endpoints for payment processing, status queries, and management operations.
 */
@RestController
@RequestMapping("/api/payment-gateway")
@CrossOrigin(origins = "*")
public class PaymentGatewayController {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentGatewayController.class);
    
    @Autowired
    private PaymentGatewayService paymentGatewayService;
    
    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;
    
    /**
     * Process a payment
     * @param request payment request
     * @return payment result
     */
    @PostMapping("/payments")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> processPayment(@Valid @RequestBody PaymentRequest request) {
        try {
            logger.info("Processing payment request: {}", request.getRequestId());
            
            PaymentResult result = paymentGatewayService.processPayment(request);
            
            return ResponseEntity.ok(result);
            
        } catch (PaymentGatewayException e) {
            logger.error("Payment processing failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e));
        } catch (Exception e) {
            logger.error("Unexpected error processing payment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("INTERNAL_ERROR", "Internal server error"));
        }
    }
    
    /**
     * Process an installment payment
     * @param parcelaId installment ID
     * @return payment result
     */
    @PostMapping("/payments/installments/{parcelaId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> processInstallmentPayment(@PathVariable Integer parcelaId) {
        try {
            logger.info("Processing installment payment for parcela: {}", parcelaId);
            
            PaymentResult result = paymentGatewayService.processInstallmentPayment(parcelaId);
            
            return ResponseEntity.ok(result);
            
        } catch (PaymentGatewayException e) {
            logger.error("Installment payment processing failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e));
        } catch (Exception e) {
            logger.error("Unexpected error processing installment payment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("INTERNAL_ERROR", "Internal server error"));
        }
    }
    
    /**
     * Query payment status
     * @param transactionId transaction ID
     * @return payment status
     */
    @GetMapping("/payments/{transactionId}/status")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> queryPaymentStatus(@PathVariable String transactionId) {
        try {
            PaymentStatusResponse status = paymentGatewayService.queryPaymentStatus(transactionId);
            return ResponseEntity.ok(status);
            
        } catch (PaymentGatewayException e) {
            logger.error("Payment status query failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e));
        } catch (Exception e) {
            logger.error("Unexpected error querying payment status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("INTERNAL_ERROR", "Internal server error"));
        }
    }
    
    /**
     * Process a refund
     * @param transactionId original transaction ID
     * @param refundRequest refund request
     * @return refund result
     */
    @PostMapping("/payments/{transactionId}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> processRefund(@PathVariable String transactionId, 
                                         @RequestBody RefundRequest refundRequest) {
        try {
            logger.info("Processing refund for transaction: {}", transactionId);
            
            RefundResult result = paymentGatewayService.processRefund(
                    transactionId, 
                    refundRequest.getAmount(), 
                    refundRequest.getReason()
            );
            
            return ResponseEntity.ok(result);
            
        } catch (PaymentGatewayException e) {
            logger.error("Refund processing failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e));
        } catch (Exception e) {
            logger.error("Unexpected error processing refund", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("INTERNAL_ERROR", "Internal server error"));
        }
    }
    
    /**
     * Cancel a payment
     * @param transactionId transaction ID
     * @return cancel result
     */
    @PostMapping("/payments/{transactionId}/cancel")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> cancelPayment(@PathVariable String transactionId) {
        try {
            logger.info("Cancelling payment: {}", transactionId);
            
            CancelResult result = paymentGatewayService.cancelPayment(transactionId);
            
            return ResponseEntity.ok(result);
            
        } catch (PaymentGatewayException e) {
            logger.error("Payment cancellation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e));
        } catch (Exception e) {
            logger.error("Unexpected error cancelling payment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("INTERNAL_ERROR", "Internal server error"));
        }
    }
    
    /**
     * Get available payment methods for an amount
     * @param amount payment amount
     * @return list of available payment methods
     */
    @GetMapping("/payment-methods")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<PaymentMethod>> getAvailablePaymentMethods(
            @RequestParam(required = false, defaultValue = "0") BigDecimal amount) {
        
        List<PaymentMethod> methods = paymentGatewayService.getAvailablePaymentMethods(amount);
        return ResponseEntity.ok(methods);
    }
    
    /**
     * Get payment providers for a payment method
     * @param paymentMethod payment method
     * @return list of supporting providers
     */
    @GetMapping("/providers")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<PaymentProvider>> getProvidersForPaymentMethod(
            @RequestParam PaymentMethod paymentMethod) {
        
        List<PaymentProvider> providers = paymentGatewayService.getProvidersForPaymentMethod(paymentMethod);
        return ResponseEntity.ok(providers);
    }
    
    /**
     * Get payment transaction by ID
     * @param transactionId transaction ID
     * @return payment transaction
     */
    @GetMapping("/transactions/{transactionId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getTransaction(@PathVariable String transactionId) {
        PaymentTransaction transaction = paymentTransactionRepository.findByTransactionId(transactionId);
        
        if (transaction == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(transaction);
    }
    
    /**
     * Get payment transactions with pagination and filtering
     * @param pageable pagination parameters
     * @param status filter by status
     * @param paymentMethod filter by payment method
     * @param providerId filter by provider ID
     * @return page of payment transactions
     */
    @GetMapping("/transactions")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<PaymentTransaction>> getTransactions(
            Pageable pageable,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) String providerId) {
        
        Page<PaymentTransaction> transactions;
        
        if (status != null) {
            transactions = paymentTransactionRepository.findByStatus(
                    PaymentStatus.valueOf(status.toUpperCase()), pageable);
        } else if (paymentMethod != null) {
            transactions = paymentTransactionRepository.findByPaymentMethod(
                    PaymentMethod.valueOf(paymentMethod.toUpperCase()), pageable);
        } else if (providerId != null) {
            transactions = paymentTransactionRepository.findByProviderId(providerId, pageable);
        } else {
            transactions = paymentTransactionRepository.findAll(pageable);
        }
        
        return ResponseEntity.ok(transactions);
    }
    
    /**
     * Get payment statistics
     * @param startDate start date (optional)
     * @param endDate end date (optional)
     * @return payment statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getPaymentStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        LocalDateTime start = startDate != null ? LocalDateTime.parse(startDate) : LocalDateTime.now().minusDays(30);
        LocalDateTime end = endDate != null ? LocalDateTime.parse(endDate) : LocalDateTime.now();
        
        Map<String, Object> statistics = new HashMap<>();
        
        // Get transaction statistics
        List<Object[]> transactionStats = paymentTransactionRepository.getTransactionStatistics(start, end);
        statistics.put("transactionStatistics", transactionStats);
        
        // Get provider statistics
        List<Object[]> providerStats = paymentTransactionRepository.getProviderStatistics(start, end);
        statistics.put("providerStatistics", providerStats);
        
        // Get payment method statistics
        List<Object[]> methodStats = paymentTransactionRepository.getPaymentMethodStatistics(start, end);
        statistics.put("paymentMethodStatistics", methodStats);
        
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * Process webhook notification
     * @param providerId provider ID
     * @param webhookRequest webhook request
     * @return webhook result
     */
    @PostMapping("/webhooks/{providerId}")
    public ResponseEntity<?> processWebhook(@PathVariable String providerId, 
                                          @RequestBody WebhookRequest webhookRequest) {
        try {
            logger.info("Processing webhook from provider: {}", providerId);
            
            WebhookResult result = paymentGatewayService.processWebhook(providerId, webhookRequest);
            
            return ResponseEntity.ok(result);
            
        } catch (PaymentGatewayException e) {
            logger.error("Webhook processing failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e));
        } catch (Exception e) {
            logger.error("Unexpected error processing webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("INTERNAL_ERROR", "Internal server error"));
        }
    }
    
    /**
     * Health check endpoint
     * @return health status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("service", "Payment Gateway");
        
        // Add basic statistics
        long totalTransactions = paymentTransactionRepository.count();
        health.put("totalTransactions", totalTransactions);
        
        return ResponseEntity.ok(health);
    }
    
    // Private helper methods
    
    private Map<String, Object> createErrorResponse(PaymentGatewayException e) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", true);
        error.put("errorCode", e.getErrorCode());
        error.put("message", e.getMessage());
        error.put("retryable", e.isRetryable());
        
        if (e.hasErrors()) {
            error.put("details", e.getErrors());
        }
        
        return error;
    }
    
    private Map<String, Object> createErrorResponse(String code, String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", true);
        error.put("errorCode", code);
        error.put("message", message);
        error.put("retryable", false);
        
        return error;
    }
}

