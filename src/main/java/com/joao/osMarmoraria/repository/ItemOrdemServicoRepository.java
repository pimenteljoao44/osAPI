package com.joao.osMarmoraria.repository;

import com.joao.osMarmoraria.domain.ItemOrdemServico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemOrdemServicoRepository extends JpaRepository<ItemOrdemServico, Integer> {

    List<ItemOrdemServico> findByOrdemServicoId(Integer ordemServicoId);

    List<ItemOrdemServico> findByProdutoId(Integer produtoId);

    @Query("SELECT ios FROM ItemOrdemServico ios LEFT JOIN FETCH ios.produto WHERE ios.ordemServicoId = :ordemServicoId")
    List<ItemOrdemServico> findByOrdemServicoIdWithProduto(@Param("ordemServicoId") Integer ordemServicoId);

    void deleteByOrdemServicoId(Integer ordemServicoId);

    @Query("SELECT SUM(ios.valorTotal) FROM ItemOrdemServico ios WHERE ios.ordemServicoId = :ordemServicoId")
    Double sumValorTotalByOrdemServicoId(@Param("ordemServicoId") Integer ordemServicoId);

    boolean existsByOrdemServicoIdAndProdutoId(Integer ordemServicoId, Integer produtoId);
}