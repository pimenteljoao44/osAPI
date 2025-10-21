package com.joao.osMarmoraria.repository;

import com.joao.osMarmoraria.domain.Peca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PecaRepository extends JpaRepository<Peca, Long> {
    void deleteByProjetoId(Integer projetoId);
    List<Peca> findByProjetoId(Integer projetoId);
    boolean existsByProjeto_Id(Integer projetoId); // Adicionado para verificar relações com Projeto
}
