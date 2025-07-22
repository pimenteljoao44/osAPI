package com.joao.osMarmoraria.gateway.dto;

import java.util.List;

/**
 * DTO para resultado de consulta de histórico de transações
 */
public class TransactionHistoryResult {

    private List<TransactionSummary> transactions;
    private Integer currentPage;
    private Integer totalPages;
    private Long totalElements;
    private Integer pageSize;
    private boolean hasNext;
    private boolean hasPrevious;
    private String sortBy;
    private String sortDirection;

    // Construtores
    public TransactionHistoryResult() {}

    public TransactionHistoryResult(List<TransactionSummary> transactions) {
        this.transactions = transactions;
    }

    // Getters e Setters
    public List<TransactionSummary> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<TransactionSummary> transactions) {
        this.transactions = transactions;
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public Long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(Long totalElements) {
        this.totalElements = totalElements;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    public boolean isHasPrevious() {
        return hasPrevious;
    }

    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }

    // Métodos utilitários
    public boolean isEmpty() {
        return transactions == null || transactions.isEmpty();
    }

    public int getTransactionCount() {
        return transactions != null ? transactions.size() : 0;
    }

    public boolean isFirstPage() {
        return currentPage != null && currentPage == 0;
    }

    public boolean isLastPage() {
        return !hasNext;
    }

    // Classe interna para resumo de transação
    public static class TransactionSummary {
        private String transactionId;
        private String providerId;
        private String providerTransactionId;
        private String paymentMethod;
        private String status;
        private java.math.BigDecimal amount;
        private String currency;
        private java.time.LocalDateTime createdAt;
        private java.time.LocalDateTime processedAt;
        private String description;
        private String customerName;
        private String customerEmail;

        // Construtores
        public TransactionSummary() {}

        public TransactionSummary(String transactionId, String status, java.math.BigDecimal amount) {
            this.transactionId = transactionId;
            this.status = status;
            this.amount = amount;
        }

        // Getters e Setters
        public String getTransactionId() {
            return transactionId;
        }

        public void setTransactionId(String transactionId) {
            this.transactionId = transactionId;
        }

        public String getProviderId() {
            return providerId;
        }

        public void setProviderId(String providerId) {
            this.providerId = providerId;
        }

        public String getProviderTransactionId() {
            return providerTransactionId;
        }

        public void setProviderTransactionId(String providerTransactionId) {
            this.providerTransactionId = providerTransactionId;
        }

        public String getPaymentMethod() {
            return paymentMethod;
        }

        public void setPaymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public java.math.BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(java.math.BigDecimal amount) {
            this.amount = amount;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public java.time.LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(java.time.LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }

        public java.time.LocalDateTime getProcessedAt() {
            return processedAt;
        }

        public void setProcessedAt(java.time.LocalDateTime processedAt) {
            this.processedAt = processedAt;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getCustomerName() {
            return customerName;
        }

        public void setCustomerName(String customerName) {
            this.customerName = customerName;
        }

        public String getCustomerEmail() {
            return customerEmail;
        }

        public void setCustomerEmail(String customerEmail) {
            this.customerEmail = customerEmail;
        }
    }
}

