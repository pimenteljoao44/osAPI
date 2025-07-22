package com.joao.osMarmoraria.repository;

import com.joao.osMarmoraria.domain.PaymentTransaction;
import com.joao.osMarmoraria.domain.enums.PaymentMethod;
import com.joao.osMarmoraria.domain.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for PaymentTransaction entity.
 * Provides data access methods for payment transaction operations.
 */
@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    
    /**
     * Find payment transaction by transaction ID
     * @param transactionId transaction ID
     * @return payment transaction or null if not found
     */
    PaymentTransaction findByTransactionId(String transactionId);
    
    /**
     * Find payment transaction by provider transaction ID
     * @param providerTransactionId provider transaction ID
     * @return payment transaction or null if not found
     */
    PaymentTransaction findByProviderTransactionId(String providerTransactionId);
    
    /**
     * Find transactions by status
     * @param status payment status
     * @param pageable pagination information
     * @return page of payment transactions
     */
    Page<PaymentTransaction> findByStatus(PaymentStatus status, Pageable pageable);
    
    /**
     * Find transactions by payment method
     * @param paymentMethod payment method
     * @param pageable pagination information
     * @return page of payment transactions
     */
    Page<PaymentTransaction> findByPaymentMethod(PaymentMethod paymentMethod, Pageable pageable);
    
    /**
     * Find transactions by provider ID
     * @param providerId provider ID
     * @param pageable pagination information
     * @return page of payment transactions
     */
    Page<PaymentTransaction> findByProviderId(String providerId, Pageable pageable);
    
    /**
     * Find transactions by date range
     * @param startDate start date
     * @param endDate end date
     * @param pageable pagination information
     * @return page of payment transactions
     */
    Page<PaymentTransaction> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    /**
     * Find transactions by amount range
     * @param minAmount minimum amount
     * @param maxAmount maximum amount
     * @param pageable pagination information
     * @return page of payment transactions
     */
    Page<PaymentTransaction> findByAmountBetween(BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable);
    
    /**
     * Find failed transactions that can be retried
     * @return list of retryable transactions
     */
    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.status = :status AND pt.retryCount < pt.maxRetries")
    List<PaymentTransaction> findRetryableTransactions(@Param("status") PaymentStatus status);
    
    /**
     * Find expired transactions
     * @param currentTime current timestamp
     * @return list of expired transactions
     */
    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.expiresAt IS NOT NULL AND pt.expiresAt < :currentTime AND pt.status NOT IN :finalStatuses")
    List<PaymentTransaction> findExpiredTransactions(@Param("currentTime") LocalDateTime currentTime, 
                                                   @Param("finalStatuses") List<PaymentStatus> finalStatuses);
    
    /**
     * Find transactions by order reference
     * @param orderReference order reference
     * @return list of payment transactions
     */
    List<PaymentTransaction> findByOrderReference(String orderReference);
    
    /**
     * Find transactions by parcela ID
     * @param parcelaId parcela ID
     * @return list of payment transactions
     */
    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.parcela.id = :parcelaId")
    List<PaymentTransaction> findByParcelaId(@Param("parcelaId") Long parcelaId);
    
    /**
     * Find transactions by compra ID
     * @param compraId compra ID
     * @return list of payment transactions
     */
    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.compra.id = :compraId")
    List<PaymentTransaction> findByCompraId(@Param("compraId") Long compraId);
    
    /**
     * Find transactions by venda ID
     * @param vendaId venda ID
     * @return list of payment transactions
     */
    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.venda.id = :vendaId")
    List<PaymentTransaction> findByVendaId(@Param("vendaId") Long vendaId);
    
    /**
     * Count transactions by status
     * @param status payment status
     * @return count of transactions
     */
    long countByStatus(PaymentStatus status);
    
    /**
     * Count transactions by payment method
     * @param paymentMethod payment method
     * @return count of transactions
     */
    long countByPaymentMethod(PaymentMethod paymentMethod);
    
    /**
     * Count transactions by provider ID
     * @param providerId provider ID
     * @return count of transactions
     */
    long countByProviderId(String providerId);
    
    /**
     * Sum processed amounts by status
     * @param status payment status
     * @return sum of processed amounts
     */
    @Query("SELECT COALESCE(SUM(pt.processedAmount), 0) FROM PaymentTransaction pt WHERE pt.status = :status")
    BigDecimal sumProcessedAmountByStatus(@Param("status") PaymentStatus status);
    
    /**
     * Sum processed amounts by payment method
     * @param paymentMethod payment method
     * @return sum of processed amounts
     */
    @Query("SELECT COALESCE(SUM(pt.processedAmount), 0) FROM PaymentTransaction pt WHERE pt.paymentMethod = :paymentMethod")
    BigDecimal sumProcessedAmountByPaymentMethod(@Param("paymentMethod") PaymentMethod paymentMethod);
    
    /**
     * Sum fees by provider ID
     * @param providerId provider ID
     * @return sum of fees
     */
    @Query("SELECT COALESCE(SUM(pt.fees), 0) FROM PaymentTransaction pt WHERE pt.providerId = :providerId")
    BigDecimal sumFeesByProviderId(@Param("providerId") String providerId);
    
    /**
     * Get transaction statistics by date range
     * @param startDate start date
     * @param endDate end date
     * @return list of transaction statistics
     */
    @Query("SELECT pt.status, COUNT(pt), COALESCE(SUM(pt.processedAmount), 0) " +
           "FROM PaymentTransaction pt " +
           "WHERE pt.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY pt.status")
    List<Object[]> getTransactionStatistics(@Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate);
    
    /**
     * Get provider performance statistics
     * @param startDate start date
     * @param endDate end date
     * @return list of provider statistics
     */
    @Query("SELECT pt.providerId, pt.status, COUNT(pt), COALESCE(SUM(pt.processedAmount), 0), COALESCE(SUM(pt.fees), 0) " +
           "FROM PaymentTransaction pt " +
           "WHERE pt.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY pt.providerId, pt.status")
    List<Object[]> getProviderStatistics(@Param("startDate") LocalDateTime startDate, 
                                       @Param("endDate") LocalDateTime endDate);
    
    /**
     * Get payment method usage statistics
     * @param startDate start date
     * @param endDate end date
     * @return list of payment method statistics
     */
    @Query("SELECT pt.paymentMethod, COUNT(pt), COALESCE(SUM(pt.processedAmount), 0) " +
           "FROM PaymentTransaction pt " +
           "WHERE pt.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY pt.paymentMethod")
    List<Object[]> getPaymentMethodStatistics(@Param("startDate") LocalDateTime startDate, 
                                            @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find transactions with high retry count
     * @param minRetryCount minimum retry count
     * @return list of transactions with high retry count
     */
    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.retryCount >= :minRetryCount")
    List<PaymentTransaction> findTransactionsWithHighRetryCount(@Param("minRetryCount") Integer minRetryCount);
    
    /**
     * Find recent transactions for monitoring
     * @param since timestamp to search from
     * @return list of recent transactions
     */
    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.updatedAt >= :since ORDER BY pt.updatedAt DESC")
    List<PaymentTransaction> findRecentTransactions(@Param("since") LocalDateTime since);
    
    /**
     * Check if transaction exists by transaction ID
     * @param transactionId transaction ID
     * @return true if exists
     */
    boolean existsByTransactionId(String transactionId);
    
    /**
     * Check if transaction exists by provider transaction ID
     * @param providerTransactionId provider transaction ID
     * @return true if exists
     */
    boolean existsByProviderTransactionId(String providerTransactionId);
}

