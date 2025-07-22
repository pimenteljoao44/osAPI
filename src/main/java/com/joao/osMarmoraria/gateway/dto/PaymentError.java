package com.joao.osMarmoraria.gateway.dto;

/**
 * Data Transfer Object for payment processing errors.
 */
public class PaymentError {
    
    /**
     * Error code
     */
    private String code;
    
    /**
     * Error message
     */
    private String message;
    
    /**
     * Error field (if applicable)
     */
    private String field;
    
    /**
     * Error type/category
     */
    private String type;
    
    // Constructors
    public PaymentError() {}
    
    public PaymentError(String code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public PaymentError(String code, String message, String field) {
        this.code = code;
        this.message = message;
        this.field = field;
    }
    
    // Getters and Setters
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getField() {
        return field;
    }
    
    public void setField(String field) {
        this.field = field;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    @Override
    public String toString() {
        return "PaymentError{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", field='" + field + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}

