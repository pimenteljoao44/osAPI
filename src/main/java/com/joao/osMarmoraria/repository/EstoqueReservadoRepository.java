package com.joao.osMarmoraria.repository;

import com.joao.osMarmoraria.domain.EstoqueReservado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EstoqueReservadoRepository extends JpaRepository<EstoqueReservado, Integer> {

    List<EstoqueReservado> findByProdutoProdIdAndAtivoTrueOrderByDataReservaDesc(Integer produtoId);

    List<EstoqueReservado> findByVendaIdAndAtivoTrue(Integer vendaId);

    List<EstoqueReservado> findByOrdemServicoIdAndAtivoTrue(Integer ordemServicoId);

    List<EstoqueReservado> findByProjetoIdAndAtivoTrue(Integer projetoId);

    @Query("SELECT COALESCE(SUM(er.quantidade), 0) FROM EstoqueReservado er WHERE er.produto.prodId = :produtoId AND er.ativo = true")
    BigDecimal calcularQuantidadeReservada(@Param("produtoId") Integer produtoId);

    @Query("SELECT er FROM EstoqueReservado er WHERE er.ativo = true AND er.dataExpiracao IS NOT NULL AND er.dataExpiracao < :dataAtual")
    List<EstoqueReservado> findReservasExpiradas(@Param("dataAtual") LocalDateTime dataAtual);

    @Query("SELECT er FROM EstoqueReservado er WHERE er.ativo = true ORDER BY er.dataReserva DESC")
    List<EstoqueReservado> findAllAtivas();

    boolean existsByVendaIdAndAtivoTrue(Integer vendaId);

    boolean existsByOrdemServicoIdAndAtivoTrue(Integer ordemServicoId);

    boolean existsByProjetoIdAndAtivoTrue(Integer projetoId);

    boolean existsByProduto_ProdId(Integer prodId); // Adicionado para verificar relações com Produto
}
