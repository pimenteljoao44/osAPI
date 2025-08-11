package com.joao.osMarmoraria.repository;

import com.joao.osMarmoraria.domain.Venda;
import com.joao.osMarmoraria.domain.enums.VendaTipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendaRepository extends JpaRepository<Venda, Integer> {

    // Métodos existentes
    List<Venda> findByVendaTipo(VendaTipo vendaTipo);


    @Query("SELECT v FROM Venda v WHERE v.cliente.cliId = :clienteId AND v.vendaTipo = :vendaTipo")
    List<Venda> findByClienteIdAndVendaTipo(@Param("clienteId") Integer clienteId, @Param("vendaTipo") VendaTipo vendaTipo);

    Optional<Venda> findByProjetoId(Integer projetoId);


    @Query("SELECT v FROM Venda v WHERE v.cliente.cliId = :cliId")
    List<Venda> findByClienteId(@Param("cliId") Integer cliId);


    @Query("SELECT v FROM Venda v WHERE v.vendaTipo = :vendaTipo AND v.projetoId IS NOT NULL")
    List<Venda> findByVendaTipoAndProjetoIdIsNotNull(@Param("vendaTipo") VendaTipo vendaTipo);

    @Query("SELECT v FROM Venda v WHERE v.vendaTipo = :vendaTipo AND v.projetoId IS NULL")
    List<Venda> findByVendaTipoAndProjetoIdIsNull(@Param("vendaTipo") VendaTipo vendaTipo);

    @Query("SELECT v FROM Venda v WHERE v.cliente.cliId = :clienteId AND v.vendaTipo = :vendaTipo AND v.projetoId IS NOT NULL")
    List<Venda> findByClienteIdAndVendaTipoAndProjetoIdIsNotNull(@Param("clienteId") Integer clienteId, @Param("vendaTipo") VendaTipo vendaTipo);

    @Query("SELECT v FROM Venda v WHERE v.vendaTipo = :vendaTipo AND v.dataFechamento IS NULL AND v.projetoId IS NOT NULL")
    List<Venda> findByVendaTipoAndDataFechamentoIsNullAndProjetoIdIsNotNull(@Param("vendaTipo") VendaTipo vendaTipo);

    @Query("SELECT v FROM Venda v WHERE v.vendaTipo = :vendaTipo AND v.dataFechamento IS NOT NULL AND v.projetoId IS NOT NULL")
    List<Venda> findByVendaTipoAndDataFechamentoIsNotNullAndProjetoIdIsNotNull(@Param("vendaTipo") VendaTipo vendaTipo);

    @Query("SELECT v FROM Venda v WHERE v.projetoId = :projetoId AND v.vendaTipo = :vendaTipo")
    Optional<Venda> findByProjetoIdAndVendaTipo(@Param("projetoId") Integer projetoId, @Param("vendaTipo") VendaTipo vendaTipo);

    @Query("SELECT COUNT(v) > 0 FROM Venda v WHERE v.projetoId = :projetoId")
    boolean existsByProjetoId(@Param("projetoId") Integer projetoId);

    // Consultas para dashboard
    @Query("SELECT COUNT(v) FROM Venda v WHERE v.vendaTipo = :vendaTipo AND v.projetoId IS NOT NULL")
    long countByVendaTipoAndProjetoIdIsNotNull(@Param("vendaTipo") VendaTipo vendaTipo);

    @Query("SELECT COUNT(v) FROM Venda v WHERE v.vendaTipo = :vendaTipo AND v.dataFechamento IS NULL AND v.projetoId IS NOT NULL")
    long countOrcamentosPendentes(@Param("vendaTipo") VendaTipo vendaTipo);

    @Query("SELECT COUNT(v) FROM Venda v WHERE v.vendaTipo = :vendaTipo AND v.dataFechamento IS NOT NULL AND v.projetoId IS NOT NULL")
    long countVendasEfetivadas(@Param("vendaTipo") VendaTipo vendaTipo);

    @Query("SELECT v FROM Venda v WHERE v.vendaTipo = :vendaTipo AND v.projetoId IS NOT NULL ORDER BY v.dataAbertura DESC")
    List<Venda> findRecentProjectSales(@Param("vendaTipo") VendaTipo vendaTipo);
    List<Venda> findByVendaTipoAndDataFechamentoIsNull(@Param("vendaTipo") VendaTipo vendaTipo);

}