package com.joao.osMarmoraria.gateway.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO para requisições de pagamento parcelado
 */
public class InstallmentPaymentRequest {

    private String transactionId;
    private BigDecimal totalAmount;
    private Integer numberOfInstallments;
    private Integer intervalDays;
    private LocalDate firstDueDate;
    private String description;
    private CustomerInfo customerInfo;
    private List<InstallmentInfo> installments;

    // Construtores
    public InstallmentPaymentRequest() {}

    public InstallmentPaymentRequest(String transactionId, BigDecimal totalAmount,
                                     Integer numberOfInstallments, Integer intervalDays) {
        this.transactionId = transactionId;
        this.totalAmount = totalAmount;
        this.numberOfInstallments = numberOfInstallments;
        this.intervalDays = intervalDays;
    }

    // Getters e Setters
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Integer getNumberOfInstallments() {
        return numberOfInstallments;
    }

    public void setNumberOfInstallments(Integer numberOfInstallments) {
        this.numberOfInstallments = numberOfInstallments;
    }

    public Integer getIntervalDays() {
        return intervalDays;
    }

    public void setIntervalDays(Integer intervalDays) {
        this.intervalDays = intervalDays;
    }

    public LocalDate getFirstDueDate() {
        return firstDueDate;
    }

    public void setFirstDueDate(LocalDate firstDueDate) {
        this.firstDueDate = firstDueDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public CustomerInfo getCustomerInfo() {
        return customerInfo;
    }

    public void setCustomerInfo(CustomerInfo customerInfo) {
        this.customerInfo = customerInfo;
    }

    public List<InstallmentInfo> getInstallments() {
        return installments;
    }

    public void setInstallments(List<InstallmentInfo> installments) {
        this.installments = installments;
    }

    // Classe interna para informações de parcela
    public static class InstallmentInfo {
        private Integer installmentNumber;
        private BigDecimal amount;
        private LocalDate dueDate;

        public InstallmentInfo() {}

        public InstallmentInfo(Integer installmentNumber, BigDecimal amount, LocalDate dueDate) {
            this.installmentNumber = installmentNumber;
            this.amount = amount;
            this.dueDate = dueDate;
        }

        public Integer getInstallmentNumber() {
            return installmentNumber;
        }

        public void setInstallmentNumber(Integer installmentNumber) {
            this.installmentNumber = installmentNumber;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public LocalDate getDueDate() {
            return dueDate;
        }

        public void setDueDate(LocalDate dueDate) {
            this.dueDate = dueDate;
        }
    }
}

