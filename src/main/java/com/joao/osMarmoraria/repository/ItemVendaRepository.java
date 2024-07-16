package com.joao.osMarmoraria.repository;

import com.joao.osMarmoraria.domain.ItemVenda;
import com.joao.osMarmoraria.domain.OrdemDeServico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemVendaRepository extends JpaRepository<ItemVenda,Integer> {
    @Query("SELECT it FROM ItemVenda it WHERE it.venda.id = ?1")
    List<ItemVenda> buscarPorvenda(Integer id);
}
