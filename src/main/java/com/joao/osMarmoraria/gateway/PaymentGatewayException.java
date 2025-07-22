package com.joao.osMarmoraria.gateway;

import com.joao.osMarmoraria.gateway.dto.PaymentError;

import java.util.ArrayList;
import java.util.List;

/**
 * Exception class for payment gateway operations.
 * Provides detailed error information for payment processing failures.
 */
public class PaymentGatewayException extends Exception {
    
    /**
     * Error code
     */
    private String errorCode;
    
    /**
     * Provider-specific error code
     */
    private String providerErrorCode;
    
    /**
     * List of detailed errors
     */
    private List<PaymentError> errors = new ArrayList<>();
    
    /**
     * Whether the error is retryable
     */
    private boolean retryable = false;
    
    /**
     * HTTP status code (if applicable)
     */
    private Integer httpStatusCode;
    
    // Constructors
    public PaymentGatewayException(String message) {
        super(message);
    }
    
    public PaymentGatewayException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public PaymentGatewayException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public PaymentGatewayException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public PaymentGatewayException(String errorCode, String message, boolean retryable) {
        super(message);
        this.errorCode = errorCode;
        this.retryable = retryable;
    }
    
    // Getters and Setters
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    public String getProviderErrorCode() {
        return providerErrorCode;
    }
    
    public void setProviderErrorCode(String providerErrorCode) {
        this.providerErrorCode = providerErrorCode;
    }
    
    public List<PaymentError> getErrors() {
        return errors;
    }
    
    public void setErrors(List<PaymentError> errors) {
        this.errors = errors;
    }
    
    public boolean isRetryable() {
        return retryable;
    }
    
    public void setRetryable(boolean retryable) {
        this.retryable = retryable;
    }
    
    public Integer getHttpStatusCode() {
        return httpStatusCode;
    }
    
    public void setHttpStatusCode(Integer httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }
    
    // Utility methods
    
    /**
     * Add an error to the exception
     * @param error PaymentError to add
     */
    public void addError(PaymentError error) {
        this.errors.add(error);
    }
    
    /**
     * Add an error with code and message
     * @param code error code
     * @param message error message
     */
    public void addError(String code, String message) {
        this.errors.add(new PaymentError(code, message));
    }
    
    /**
     * Check if there are any errors
     * @return true if errors exist
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
    
    /**
     * Get the first error message
     * @return first error message or null if no errors
     */
    public String getFirstErrorMessage() {
        return errors.isEmpty() ? null : errors.get(0).getMessage();
    }
    
    /**
     * Get the first error code
     * @return first error code or null if no errors
     */
    public String getFirstErrorCode() {
        return errors.isEmpty() ? null : errors.get(0).getCode();
    }
    
    // Static factory methods for common error scenarios
    
    /**
     * Create exception for invalid payment request
     * @param message error message
     * @return PaymentGatewayException
     */
    public static PaymentGatewayException invalidRequest(String message) {
        return new PaymentGatewayException("INVALID_REQUEST", message, false);
    }
    
    /**
     * Create exception for authentication failure
     * @param message error message
     * @return PaymentGatewayException
     */
    public static PaymentGatewayException authenticationFailed(String message) {
        return new PaymentGatewayException("AUTHENTICATION_FAILED", message, false);
    }
    
    /**
     * Create exception for insufficient funds
     * @param message error message
     * @return PaymentGatewayException
     */
    public static PaymentGatewayException insufficientFunds(String message) {
        return new PaymentGatewayException("INSUFFICIENT_FUNDS", message, false);
    }
    
    /**
     * Create exception for network/connection errors
     * @param message error message
     * @param cause underlying cause
     * @return PaymentGatewayException
     */
    public static PaymentGatewayException networkError(String message, Throwable cause) {
        return new PaymentGatewayException("NETWORK_ERROR", message, cause);
    }
    
    /**
     * Create exception for provider unavailable
     * @param message error message
     * @return PaymentGatewayException
     */
    public static PaymentGatewayException providerUnavailable(String message) {
        return new PaymentGatewayException("PROVIDER_UNAVAILABLE", message, true);
    }
    
    /**
     * Create exception for timeout
     * @param message error message
     * @return PaymentGatewayException
     */
    public static PaymentGatewayException timeout(String message) {
        return new PaymentGatewayException("TIMEOUT", message, true);
    }
    
    /**
     * Create exception for rate limit exceeded
     * @param message error message
     * @return PaymentGatewayException
     */
    public static PaymentGatewayException rateLimitExceeded(String message) {
        return new PaymentGatewayException("RATE_LIMIT_EXCEEDED", message, true);
    }
    
    /**
     * Create exception for configuration error
     * @param message error message
     * @return PaymentGatewayException
     */
    public static PaymentGatewayException configurationError(String message) {
        return new PaymentGatewayException("CONFIGURATION_ERROR", message, false);
    }
    
    @Override
    public String toString() {
        return "PaymentGatewayException{" +
                "errorCode='" + errorCode + '\'' +
                ", providerErrorCode='" + providerErrorCode + '\'' +
                ", message='" + getMessage() + '\'' +
                ", retryable=" + retryable +
                ", errorsCount=" + errors.size() +
                '}';
    }
}

