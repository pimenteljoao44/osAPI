package com.joao.osMarmoraria.repository;

import com.joao.osMarmoraria.domain.ContaPagar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ContaPagarRepository extends JpaRepository<ContaPagar, Integer> {

    @Query("SELECT cp FROM ContaPagar cp LEFT JOIN FETCH cp.compra c LEFT JOIN FETCH c.fornecedor WHERE cp.id = :id")
    ContaPagar findByIdWithDetails(@Param("id") Integer id);

    @Query("SELECT cp FROM ContaPagar cp LEFT JOIN FETCH cp.compra c LEFT JOIN FETCH c.fornecedor")
    List<ContaPagar> findAllWithDetails();

    List<ContaPagar> findByStatus(String status);

    @Query("SELECT cp FROM ContaPagar cp WHERE cp.compra.fornecedor.id = :fornecedorId")
    List<ContaPagar> findByFornecedorId(@Param("fornecedorId") Integer fornecedorId);

    @Query("SELECT cp FROM ContaPagar cp WHERE cp.compra.comprId = :compraId")
    List<ContaPagar> findByCompraId(@Param("compraId") Integer compraId);


    @Query("SELECT cp FROM ContaPagar cp WHERE cp.dataVencimento BETWEEN :dataInicio AND :dataFim")
    List<ContaPagar> findByPeriodoVencimento(@Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);

    @Query("SELECT cp FROM ContaPagar cp WHERE cp.dataVencimento < :data AND cp.status = 'PENDENTE'")
    List<ContaPagar> findContasVencidas(@Param("data") LocalDate data);

    @Query("SELECT cp FROM ContaPagar cp WHERE cp.dataVencimento BETWEEN :dataInicio AND :dataFim AND cp.status = 'PENDENTE'")
    List<ContaPagar> findContasVencendoNoPeriodo(@Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);

    @Query("SELECT SUM(cp.valor) FROM ContaPagar cp WHERE cp.status = :status")
    BigDecimal sumByStatus(@Param("status") String status);

    @Query("SELECT SUM(cp.valor) FROM ContaPagar cp WHERE cp.status = 'PENDENTE' AND cp.dataVencimento < :data")
    BigDecimal sumContasVencidas(@Param("data") LocalDate data);

    @Query("SELECT COUNT(cp) FROM ContaPagar cp WHERE cp.status = :status")
    Integer countByStatus(@Param("status") String status);

    @Query("SELECT cp FROM ContaPagar cp WHERE cp.dataPagamento BETWEEN :dataInicio AND :dataFim")
    List<ContaPagar> findByPeriodoPagamento(@Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);
}
