package com.joao.osMarmoraria.repository;

import com.joao.osMarmoraria.domain.Cliente;
import com.joao.osMarmoraria.domain.Fornecedor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface FornecedorRepository extends JpaRepository<Fornecedor,Integer> {
    @EntityGraph(attributePaths = {"pessoa", "pessoa.enderecos", "pessoa.enderecos.cidade"})
    @Query("SELECT DISTINCT c FROM Fornecedor c")
    List<Cliente> findAllWithEnderecosAndCidades();
}
