package com.joao.osMarmoraria.repository;

import com.joao.osMarmoraria.domain.Estado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstadoRepository extends JpaRepository<Estado,Integer> {
    boolean existsByNomeOrSigla(String nome, String sigla);

    Optional<Estado> findBySigla(String sigla);
    Optional<Estado> findByNomeAndSigla(String nome, String sigla);
}
