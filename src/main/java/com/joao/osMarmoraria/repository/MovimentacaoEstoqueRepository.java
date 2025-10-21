package com.joao.osMarmoraria.repository;

import com.joao.osMarmoraria.domain.MovimentacaoEstoque;
import com.joao.osMarmoraria.domain.enums.TipoMovimentacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Integer> {

    List<MovimentacaoEstoque> findByProdutoProdIdOrderByDataMovimentacaoDesc(Integer produtoId);

    List<MovimentacaoEstoque> findByTipoOrderByDataMovimentacaoDesc(TipoMovimentacao tipo);

    List<MovimentacaoEstoque> findByVendaIdOrderByDataMovimentacaoDesc(Integer vendaId);

    List<MovimentacaoEstoque> findByOrdemServicoIdOrderByDataMovimentacaoDesc(Integer ordemServicoId);

    List<MovimentacaoEstoque> findByProjetoIdOrderByDataMovimentacaoDesc(Integer projetoId);

    @Query("SELECT m FROM MovimentacaoEstoque m WHERE m.dataMovimentacao BETWEEN :dataInicio AND :dataFim ORDER BY m.dataMovimentacao DESC")
    List<MovimentacaoEstoque> findByPeriodo(@Param("dataInicio") LocalDateTime dataInicio,
                                            @Param("dataFim") LocalDateTime dataFim);

    @Query("SELECT m FROM MovimentacaoEstoque m " + "WHERE m.produto.prodId = :produtoId " +
            "AND m.dataMovimentacao BETWEEN :dataInicio AND :dataFim " +
            "ORDER BY m.dataMovimentacao DESC")
    List<MovimentacaoEstoque> findByProdutoAndPeriodo(
            @Param("produtoId") Integer produtoId,
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim);

    @Query("SELECT COALESCE(SUM(CASE WHEN m.tipo IN (\'ENTRADA_COMPRA\', \'ENTRADA_AJUSTE\', \'ENTRADA_DEVOLUCAO\', \'TRANSFERENCIA_ENTRADA\', \'LIBERACAO_RESERVA\') THEN m.quantidade ELSE 0 END), 0) - " +
            "COALESCE(SUM(CASE WHEN m.tipo IN (\'SAIDA_VENDA\', \'SAIDA_AJUSTE\', \'SAIDA_PERDA\', \'TRANSFERENCIA_SAIDA\', \'BAIXA_ORDEM_SERVICO\', \'RESERVA_VENDA\') THEN m.quantidade ELSE 0 END), 0) " +
            "FROM MovimentacaoEstoque m WHERE m.produto.prodId = :produtoId")
    BigDecimal calcularEstoqueAtualPorMovimentacoes(@Param("produtoId") Integer produtoId);

    boolean existsByProduto_ProdId(Integer prodId); // Adicionado para verificar relações com Produto
    boolean existsByVendaId(Integer vendaId); // Adicionado para verificar relações com Venda (campo direto)
}
