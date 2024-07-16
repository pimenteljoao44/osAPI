package com.joao.osMarmoraria.repository;

import com.joao.osMarmoraria.domain.Grupo;
import com.joao.osMarmoraria.domain.OrdemDeServico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GrupoRepository extends JpaRepository<Grupo,Integer> {

    boolean existsByNome(String nome);
}
