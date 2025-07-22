package com.joao.osMarmoraria.gateway.dto;

import com.joao.osMarmoraria.domain.enums.PaymentMethod;
import com.joao.osMarmoraria.domain.enums.PaymentStatus;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para requisição de histórico de transações
 */
public class TransactionHistoryRequest {

    private String customerId;
    private String providerId;
    private List<PaymentMethod> paymentMethods;
    private List<PaymentStatus> statuses;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDirection; // ASC, DESC
    private String searchTerm;
    private String transactionIdFilter;

    // Construtores
    public TransactionHistoryRequest() {
        this.page = 0;
        this.size = 20;
        this.sortBy = "createdAt";
        this.sortDirection = "DESC";
    }

    public TransactionHistoryRequest(String customerId) {
        this();
        this.customerId = customerId;
    }

    // Getters e Setters
    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public List<PaymentMethod> getPaymentMethods() {
        return paymentMethods;
    }

    public void setPaymentMethods(List<PaymentMethod> paymentMethods) {
        this.paymentMethods = paymentMethods;
    }

    public List<PaymentStatus> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<PaymentStatus> statuses) {
        this.statuses = statuses;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
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

    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public String getTransactionIdFilter() {
        return transactionIdFilter;
    }

    public void setTransactionIdFilter(String transactionIdFilter) {
        this.transactionIdFilter = transactionIdFilter;
    }

    // Métodos utilitários
    public boolean hasDateFilter() {
        return startDate != null || endDate != null;
    }

    public boolean hasStatusFilter() {
        return statuses != null && !statuses.isEmpty();
    }

    public boolean hasPaymentMethodFilter() {
        return paymentMethods != null && !paymentMethods.isEmpty();
    }

    public boolean hasSearchTerm() {
        return searchTerm != null && !searchTerm.trim().isEmpty();
    }

    public boolean isDescendingSort() {
        return "DESC".equalsIgnoreCase(sortDirection);
    }

    public boolean isAscendingSort() {
        return "ASC".equalsIgnoreCase(sortDirection);
    }

    // Builder pattern para facilitar construção
    public static class Builder {
        private TransactionHistoryRequest request;

        public Builder() {
            request = new TransactionHistoryRequest();
        }

        public Builder customerId(String customerId) {
            request.setCustomerId(customerId);
            return this;
        }

        public Builder providerId(String providerId) {
            request.setProviderId(providerId);
            return this;
        }

        public Builder paymentMethods(List<PaymentMethod> methods) {
            request.setPaymentMethods(methods);
            return this;
        }

        public Builder statuses(List<PaymentStatus> statuses) {
            request.setStatuses(statuses);
            return this;
        }

        public Builder dateRange(LocalDateTime start, LocalDateTime end) {
            request.setStartDate(start);
            request.setEndDate(end);
            return this;
        }

        public Builder page(int page, int size) {
            request.setPage(page);
            request.setSize(size);
            return this;
        }

        public Builder sort(String sortBy, String direction) {
            request.setSortBy(sortBy);
            request.setSortDirection(direction);
            request.setSortDirection(direction);
            return this;
        }

        public Builder search(String searchTerm) {
            request.setSearchTerm(searchTerm);
            return this;
        }

        public TransactionHistoryRequest build() {
            return request;
        }
    }
}