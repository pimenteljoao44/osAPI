package com.joao.osMarmoraria.repository;

import com.joao.osMarmoraria.domain.PaymentProvider;
import com.joao.osMarmoraria.domain.enums.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for PaymentProvider entity.
 * Provides data access methods for payment provider operations.
 */
@Repository
public interface PaymentProviderRepository extends JpaRepository<PaymentProvider, Long> {
    
    /**
     * Find payment provider by provider ID
     * @param providerId provider ID
     * @return payment provider or null if not found
     */
    PaymentProvider findByProviderId(String providerId);
    
    /**
     * Find payment provider by provider name
     * @param providerName provider name
     * @return payment provider or null if not found
     */
    PaymentProvider findByProviderName(String providerName);
    
    /**
     * Find all enabled providers ordered by priority
     * @return list of enabled providers
     */
    List<PaymentProvider> findByEnabledTrueOrderByPriority();
    
    /**
     * Find enabled providers that support a specific payment method
     * @param paymentMethod payment method
     * @return list of supporting providers
     */
    @Query("SELECT pp FROM PaymentProvider pp WHERE pp.enabled = true AND :paymentMethod MEMBER OF pp.supportedPaymentMethods ORDER BY pp.priority")
    List<PaymentProvider> findByEnabledTrueAndSupportedPaymentMethodsContaining(@Param("paymentMethod") PaymentMethod paymentMethod);
    
    /**
     * Find enabled providers that support a specific payment method ordered by priority
     * @param paymentMethod payment method
     * @return list of supporting providers ordered by priority
     */
    @Query("SELECT pp FROM PaymentProvider pp WHERE pp.enabled = true AND :paymentMethod MEMBER OF pp.supportedPaymentMethods ORDER BY pp.priority")
    List<PaymentProvider> findByEnabledTrueAndSupportedPaymentMethodsContainingOrderByPriority(@Param("paymentMethod") PaymentMethod paymentMethod);
    
    /**
     * Find providers by environment
     * @param environment environment name
     * @return list of providers
     */
    List<PaymentProvider> findByEnvironment(String environment);
    
    /**
     * Find enabled providers by environment
     * @param environment environment name
     * @return list of enabled providers
     */
    List<PaymentProvider> findByEnabledTrueAndEnvironment(String environment);
    
    /**
     * Find providers that support a specific amount range
     * @param amount amount to check
     * @return list of providers that support the amount
     */
    @Query("SELECT pp FROM PaymentProvider pp WHERE pp.enabled = true AND " +
           "(pp.minAmount IS NULL OR pp.minAmount <= :amount) AND " +
           "(pp.maxAmount IS NULL OR pp.maxAmount >= :amount)")
    List<PaymentProvider> findProvidersSupportingAmount(@Param("amount") BigDecimal amount);
    
    /**
     * Find providers that support installments
     * @return list of providers supporting installments
     */
    List<PaymentProvider> findByEnabledTrueAndSupportsInstallmentsTrue();
    
    /**
     * Find providers that support webhooks
     * @return list of providers supporting webhooks
     */
    List<PaymentProvider> findByEnabledTrueAndSupportsWebhooksTrue();
    
    /**
     * Find providers that support refunds
     * @return list of providers supporting refunds
     */
    List<PaymentProvider> findByEnabledTrueAndSupportsRefundsTrue();
    
    /**
     * Find providers with healthy status
     * @return list of healthy providers
     */
    @Query("SELECT pp FROM PaymentProvider pp WHERE pp.enabled = true AND pp.healthStatus = 'HEALTHY'")
    List<PaymentProvider> findHealthyProviders();
    
    /**
     * Find providers by priority range
     * @param minPriority minimum priority
     * @param maxPriority maximum priority
     * @return list of providers in priority range
     */
    List<PaymentProvider> findByEnabledTrueAndPriorityBetweenOrderByPriority(Integer minPriority, Integer maxPriority);
    
    /**
     * Count enabled providers
     * @return count of enabled providers
     */
    long countByEnabledTrue();
    
    /**
     * Count providers by environment
     * @param environment environment name
     * @return count of providers
     */
    long countByEnvironment(String environment);
    
    /**
     * Count providers supporting a payment method
     * @param paymentMethod payment method
     * @return count of supporting providers
     */
    @Query("SELECT COUNT(pp) FROM PaymentProvider pp WHERE pp.enabled = true AND :paymentMethod MEMBER OF pp.supportedPaymentMethods")
    long countProvidersSupportingPaymentMethod(@Param("paymentMethod") PaymentMethod paymentMethod);
    
    /**
     * Find providers with lowest fees for a payment method
     * @param paymentMethod payment method
     * @param amount transaction amount
     * @return list of providers ordered by calculated fees
     */
    @Query("SELECT pp FROM PaymentProvider pp WHERE pp.enabled = true AND :paymentMethod MEMBER OF pp.supportedPaymentMethods AND " +
           "(pp.minAmount IS NULL OR pp.minAmount <= :amount) AND " +
           "(pp.maxAmount IS NULL OR pp.maxAmount >= :amount) " +
           "ORDER BY (COALESCE(pp.feePercentage, 0) * :amount + COALESCE(pp.fixedFee, 0))")
    List<PaymentProvider> findProvidersWithLowestFees(@Param("paymentMethod") PaymentMethod paymentMethod, 
                                                     @Param("amount") BigDecimal amount);
    
    /**
     * Find providers by timeout range
     * @param minTimeout minimum timeout in seconds
     * @param maxTimeout maximum timeout in seconds
     * @return list of providers
     */
    List<PaymentProvider> findByEnabledTrueAndTimeoutSecondsBetween(Integer minTimeout, Integer maxTimeout);
    
    /**
     * Find providers that need health check
     * @param maxAge maximum age since last test in hours
     * @return list of providers needing health check
     */
    @Query("SELECT pp FROM PaymentProvider pp WHERE pp.enabled = true AND " +
           "(pp.lastTestedAt IS NULL OR pp.lastTestedAt < :cutoffTime)")
    List<PaymentProvider> findProvidersNeedingHealthCheck(@Param("cutoffTime") java.time.LocalDateTime cutoffTime);
    
    /**
     * Find providers with configuration issues
     * @return list of providers with potential issues
     */
    @Query("SELECT pp FROM PaymentProvider pp WHERE pp.enabled = true AND " +
           "(pp.apiUrl IS NULL OR pp.healthStatus != 'HEALTHY' OR SIZE(pp.credentials) = 0)")
    List<PaymentProvider> findProvidersWithConfigurationIssues();
    
    /**
     * Check if provider ID exists
     * @param providerId provider ID
     * @return true if exists
     */
    boolean existsByProviderId(String providerId);
    
    /**
     * Check if provider name exists
     * @param providerName provider name
     * @return true if exists
     */
    boolean existsByProviderName(String providerName);
    
    /**
     * Find providers ordered by success rate (would require transaction data)
     * This is a placeholder for a more complex query that would join with transaction data
     * @return list of providers ordered by performance
     */
    @Query("SELECT pp FROM PaymentProvider pp WHERE pp.enabled = true ORDER BY pp.priority")
    List<PaymentProvider> findProvidersByPerformance();
}

