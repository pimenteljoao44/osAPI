package com.joao.osMarmoraria.repository;

import com.joao.osMarmoraria.domain.ItemVenda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemVendaRepository extends JpaRepository<ItemVenda,Integer> {
    @Query("SELECT it FROM ItemVenda it WHERE it.venda.id = ?1")
    List<ItemVenda> buscarPorvenda(Integer id);

    boolean existsByProduto_ProdId(Integer prodId); // Adicionado para verificar relações com Produto
    boolean existsByVenda_VenId(Integer vendaId); // Adicionado para verificar relações com Venda
}
