package com.joao.osMarmoraria.repository;

import com.joao.osMarmoraria.domain.Parcela;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ParcelaRepository extends JpaRepository<Parcela, Integer> {
    
    // Buscar parcelas por ContaPagar
    List<Parcela> findByContaPagarIdOrderByNumeroParcela(Integer contaPagarId);
    
    // Buscar parcelas por ContaReceber
    List<Parcela> findByContaReceberIdOrderByNumeroParcela(Integer contaReceberId);
    
    // Buscar parcelas por status
    List<Parcela> findByStatusOrderByDataVencimento(String status);
    
    // Buscar parcelas vencidas
    @Query("SELECT p FROM Parcela p WHERE p.status = 'PENDENTE' AND p.dataVencimento < :dataAtual ORDER BY p.dataVencimento")
    List<Parcela> findParcelasVencidas(@Param("dataAtual") LocalDate dataAtual);
    
    // Buscar parcelas por período de vencimento
    @Query("SELECT p FROM Parcela p WHERE p.dataVencimento BETWEEN :dataInicio AND :dataFim ORDER BY p.dataVencimento")
    List<Parcela> findByDataVencimentoBetween(@Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);
    
    // Buscar parcelas pendentes por período
    @Query("SELECT p FROM Parcela p WHERE p.status = 'PENDENTE' AND p.dataVencimento BETWEEN :dataInicio AND :dataFim ORDER BY p.dataVencimento")
    List<Parcela> findParcelasPendentesPorPeriodo(@Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);
    
    // Buscar próximas parcelas a vencer (próximos N dias)
    @Query("SELECT p FROM Parcela p WHERE p.status = 'PENDENTE' AND p.dataVencimento BETWEEN :dataAtual AND :dataLimite ORDER BY p.dataVencimento")
    List<Parcela> findProximasParcelasAVencer(@Param("dataAtual") LocalDate dataAtual, @Param("dataLimite") LocalDate dataLimite);
    
    // Contar parcelas por status
    Long countByStatus(String status);
    
    // Contar parcelas pagas de uma conta a pagar
    @Query("SELECT COUNT(p) FROM Parcela p WHERE p.contaPagar.id = :contaPagarId AND p.status = 'PAGO'")
    Long countParcelasPagasPorContaPagar(@Param("contaPagarId") Integer contaPagarId);
    
    // Contar parcelas recebidas de uma conta a receber
    @Query("SELECT COUNT(p) FROM Parcela p WHERE p.contaReceber.id = :contaReceberId AND p.status = 'PAGO'")
    Long countParcelasRecebidasPorContaReceber(@Param("contaReceberId") Integer contaReceberId);
    
    // Buscar parcelas com detalhes completos (join fetch)
    @Query("SELECT p FROM Parcela p LEFT JOIN FETCH p.contaPagar cp LEFT JOIN FETCH p.contaReceber cr WHERE p.id = :id")
    Parcela findByIdWithDetails(@Param("id") Integer id);
    
    // Buscar todas as parcelas com detalhes
    @Query("SELECT p FROM Parcela p LEFT JOIN FETCH p.contaPagar cp LEFT JOIN FETCH p.contaReceber cr ORDER BY p.dataVencimento")
    List<Parcela> findAllWithDetails();
    
    // Buscar parcelas por compra (através de ContaPagar)
    @Query("SELECT p FROM Parcela p JOIN p.contaPagar cp WHERE cp.compra.comprId = :compraId ORDER BY p.numeroParcela")
    List<Parcela> findByCompraId(@Param("compraId") Integer compraId);
    
    // Buscar parcelas por venda (através de ContaReceber)
    @Query("SELECT p FROM Parcela p JOIN p.contaReceber cr WHERE cr.venda.venId = :vendaId ORDER BY p.numeroParcela")
    List<Parcela> findByVendaId(@Param("vendaId") Integer vendaId);
}

