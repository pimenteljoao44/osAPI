package com.joao.osMarmoraria.repository;

import com.joao.osMarmoraria.domain.ProjetoAcabamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjetoAcabamentoRepository extends JpaRepository<ProjetoAcabamento, Integer> {
    boolean existsByProjeto_Id(Integer projetoId);
}
