package com.joao.osMarmoraria.repository;

import com.joao.osMarmoraria.domain.ProjetoMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjetoMaterialRepository extends JpaRepository<ProjetoMaterial, Integer> {
    boolean existsByProduto_ProdId(Integer produtoId);
}
