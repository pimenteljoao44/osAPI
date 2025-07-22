package com.joao.osMarmoraria.repository;

import com.joao.osMarmoraria.domain.Cidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CidadeRepository extends JpaRepository<Cidade,Integer> {
    Optional<Cidade> findByNome(String nome);
    Optional<Cidade> findByNomeAndUf(String nome, String uf);
}
