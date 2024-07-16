package com.joao.osMarmoraria.repository;

import com.joao.osMarmoraria.domain.OrdemDeServico;
import com.joao.osMarmoraria.domain.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto,Integer> {
    boolean existsByNome(String nome);
}
