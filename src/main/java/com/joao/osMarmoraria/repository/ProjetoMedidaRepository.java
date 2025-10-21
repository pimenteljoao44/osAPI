package com.joao.osMarmoraria.repository;

import com.joao.osMarmoraria.domain.ProjetoMedida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjetoMedidaRepository extends JpaRepository<ProjetoMedida, Integer> {

    /**
     * Busca todas as medidas de um projeto específico
     */
    List<ProjetoMedida> findByProjetoIdOrderByNome(Integer projetoId);

    /**
     * Busca medidas por projeto e nome (busca parcial)
     */
    @Query("SELECT pm FROM ProjetoMedida pm WHERE pm.projetoId = :projetoId AND LOWER(pm.nome) LIKE LOWER(CONCAT('%', :nome, '%'))")
    List<ProjetoMedida> findByProjetoIdAndNomeContainingIgnoreCase(@Param("projetoId") Integer projetoId, @Param("nome") String nome);

    /**
     * Conta quantas medidas um projeto possui
     */
    long countByProjetoId(Integer projetoId);

    /**
     * Remove todas as medidas de um projeto
     */
    void deleteByProjetoId(Integer projetoId);

    /**
     * Verifica se existe uma medida com o nome específico no projeto
     */
    boolean existsByProjetoIdAndNomeIgnoreCase(Integer projetoId, String nome);

    /**
     * Busca medidas que possuem coordenadas (para visualização)
     */
    @Query("SELECT pm FROM ProjetoMedida pm WHERE pm.projetoId = :projetoId AND pm.coordenadaX IS NOT NULL AND pm.coordenadaY IS NOT NULL")
    List<ProjetoMedida> findMedidasComCoordenadas(@Param("projetoId") Integer projetoId);

    boolean existsByProjeto_Id(Integer projetoId); // Adicionado para verificar relações com Projeto
}
