package com.joao.osMarmoraria.repository;

import com.joao.osMarmoraria.domain.ContaReceber;
import com.joao.osMarmoraria.domain.Venda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ContaReceberRepository extends JpaRepository<ContaReceber, Integer> {

    @Query("SELECT cr FROM ContaReceber cr " +
            "LEFT JOIN FETCH cr.venda v " +
            "LEFT JOIN FETCH v.cliente " +
            "LEFT JOIN FETCH v.projeto p " +
            "LEFT JOIN FETCH p.usuarioCriacao " +
            "WHERE cr.id = :id")
    ContaReceber findByIdWithDetails(@Param("id") Integer id);

    @Query("SELECT cr FROM ContaReceber cr " +
            "LEFT JOIN FETCH cr.venda v " +
            "LEFT JOIN FETCH v.cliente " +
            "LEFT JOIN FETCH v.projeto p " +
            "LEFT JOIN FETCH p.usuarioCriacao")
    List<ContaReceber> findAllWithDetails();

    List<ContaReceber> findByStatus(String status);

    @Query("SELECT cr FROM ContaReceber cr WHERE cr.venda.cliente.cliId = :clienteId OR cr.projeto.cliente.cliId = :clienteId")
    List<ContaReceber> findByClienteId(@Param("clienteId") Integer clienteId);

    List<ContaReceber> findByProjetoId(Integer projetoId);

    @Query("SELECT cr FROM ContaReceber cr WHERE cr.dataVencimento BETWEEN :dataInicio AND :dataFim")
    List<ContaReceber> findByPeriodoVencimento(@Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);

    @Query("SELECT cr FROM ContaReceber cr WHERE cr.dataVencimento < :data AND cr.status = 'PENDENTE'")
    List<ContaReceber> findContasVencidas(@Param("data") LocalDate data);

    @Query("SELECT cr FROM ContaReceber cr WHERE cr.dataVencimento BETWEEN :dataInicio AND :dataFim AND cr.status = 'PENDENTE'")
    List<ContaReceber> findContasVencendoNoPeriodo(@Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);

    @Query("SELECT SUM(cr.valor) FROM ContaReceber cr WHERE cr.status = :status")
    BigDecimal sumByStatus(@Param("status") String status);

    @Query("SELECT SUM(cr.valor) FROM ContaReceber cr WHERE cr.status = 'PENDENTE' AND cr.dataVencimento < :data")
    BigDecimal sumContasVencidas(@Param("data") LocalDate data);

    @Query("SELECT COUNT(cr) FROM ContaReceber cr WHERE cr.status = :status")
    Integer countByStatus(@Param("status") String status);

    @Query("SELECT cr FROM ContaReceber cr WHERE cr.dataPagamento BETWEEN :dataInicio AND :dataFim")
    List<ContaReceber> findByPeriodoRecebimento(@Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);

    @Query("SELECT SUM(cr.valor) FROM ContaReceber cr WHERE cr.dataPagamento BETWEEN :dataInicio AND :dataFim AND cr.status = 'RECEBIDO'")
    BigDecimal sumRecebidoNoPeriodo(@Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);

    @Query("SELECT SUM(cr.valor) FROM ContaReceber cr WHERE cr.dataVencimento BETWEEN :dataInicio AND :dataFim")
    BigDecimal sumVencimentoNoPeriodo(@Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);

    List<ContaReceber> findByVenda(Venda venda);
}