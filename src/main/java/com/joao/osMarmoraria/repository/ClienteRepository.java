package com.joao.osMarmoraria.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.joao.osMarmoraria.domain.Cliente;

import java.util.List;


@Repository
public interface ClienteRepository extends JpaRepository<Cliente,Integer> {
    @EntityGraph(attributePaths = {"pessoa", "pessoa.enderecos", "pessoa.enderecos.cidade"})
    @Query("SELECT DISTINCT c FROM Cliente c")
    List<Cliente> findAllWithEnderecosAndCidades();
}
