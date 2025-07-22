package com.joao.osMarmoraria.repository;


import com.joao.osMarmoraria.domain.Projeto;
import com.joao.osMarmoraria.domain.Venda;
import com.joao.osMarmoraria.domain.enums.TipoProjeto;
import com.joao.osMarmoraria.domain.enums.VendaTipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VendaRepository extends JpaRepository<Venda,Integer> {
    List<Venda> findByVendaTipo(VendaTipo vendaTipo);

    @Query("SELECT v FROM Venda v WHERE v.cliente.id = :clienteId AND v.vendaTipo = :vendaTipo")
    List<Venda> findByClienteIdAndVendaTipo(@Param("clienteId") Integer clienteId, @Param("vendaTipo") VendaTipo vendaTipo);

}
