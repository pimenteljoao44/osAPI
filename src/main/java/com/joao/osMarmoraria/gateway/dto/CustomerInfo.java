package com.joao.osMarmoraria.gateway.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for customer information in payment requests.
 */
public class CustomerInfo {
    
    /**
     * Customer's full name
     */
    @NotBlank
    @Size(max = 100)
    private String name;
    
    /**
     * Customer's email address
     */
    @Email
    @Size(max = 100)
    private String email;
    
    /**
     * Customer's phone number
     */
    @Size(max = 20)
    private String phone;
    
    /**
     * Customer's document number (CPF/CNPJ)
     */
    @Size(max = 20)
    private String documentNumber;
    
    /**
     * Document type (CPF, CNPJ, etc.)
     */
    @Size(max = 10)
    private String documentType;
    
    /**
     * Customer's date of birth (for individuals)
     */
    private String dateOfBirth;
    
    /**
     * Customer type (INDIVIDUAL, BUSINESS)
     */
    private String customerType;
    
    /**
     * Customer's IP address (for fraud detection)
     */
    private String ipAddress;
    
    /**
     * Customer's user agent (for fraud detection)
     */
    private String userAgent;
    
    // Constructors
    public CustomerInfo() {}
    
    public CustomerInfo(String name, String email, String documentNumber) {
        this.name = name;
        this.email = email;
        this.documentNumber = documentNumber;
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getDocumentNumber() {
        return documentNumber;
    }
    
    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }
    
    public String getDocumentType() {
        return documentType;
    }
    
    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }
    
    public String getDateOfBirth() {
        return dateOfBirth;
    }
    
    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
    
    public String getCustomerType() {
        return customerType;
    }
    
    public void setCustomerType(String customerType) {
        this.customerType = customerType;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    @Override
    public String toString() {
        return "CustomerInfo{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", documentNumber='" + documentNumber + '\'' +
                ", documentType='" + documentType + '\'' +
                ", customerType='" + customerType + '\'' +
                '}';
    }
}

