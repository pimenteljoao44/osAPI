package com.joao.osMarmoraria.repository;

import com.joao.osMarmoraria.domain.OrdemDeServico;
import com.joao.osMarmoraria.domain.Venda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VendaRepository extends JpaRepository<Venda,Integer> {

}
