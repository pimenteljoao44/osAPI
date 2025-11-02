package com.joao.osMarmoraria.repository;

import com.joao.osMarmoraria.domain.Produto;
import com.joao.osMarmoraria.domain.ProjetoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjetoItemRepository extends JpaRepository<ProjetoItem, Integer> {

    List<ProjetoItem> findByProjetoId(Integer projetoId);

    List<ProjetoItem> findByProdutoId(Integer produtoId);

    @Query("SELECT pi FROM ProjetoItem pi LEFT JOIN FETCH pi.produto WHERE pi.projetoId = :projetoId")
    List<ProjetoItem> findByProjetoIdWithProduto(@Param("projetoId") Integer projetoId);

    void deleteByProjetoId(Integer projetoId);

    @Query("SELECT SUM(pi.valorTotal) FROM ProjetoItem pi WHERE pi.projetoId = :projetoId")
    Double sumValorTotalByProjetoId(@Param("projetoId") Integer projetoId);

    @Query("SELECT COUNT(pi) FROM ProjetoItem pi WHERE pi.projetoId = :projetoId")
    Integer countByProjetoId(@Param("projetoId") Integer projetoId);

    boolean existsByProjetoIdAndProdutoId(Integer projetoId, Integer produtoId);
    boolean existsByProduto(Produto produto);
    boolean existsByProdutoId(Integer produtoId); // Adicionado para verificar relações com Produto
}
