package com.joao.osMarmoraria.repository;

import com.joao.osMarmoraria.domain.ItemCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemCompraRepository extends JpaRepository<ItemCompra,Integer> {
    @Query("SELECT it FROM ItemCompra it WHERE it.compra.comprId = ?1")
    List<ItemCompra> buscarPorvenda(Integer id);
}
