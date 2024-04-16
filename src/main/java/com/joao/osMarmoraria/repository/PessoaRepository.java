package com.joao.osMarmoraria.repository;

import com.joao.osMarmoraria.domain.Cliente;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import com.joao.osMarmoraria.domain.Pessoa;

import java.util.List;

@Repository
public interface PessoaRepository extends JpaRepository<Pessoa,Integer> {

	@Query("SELECT obj FROM PessoaFisica obj WHERE obj.cpf =:cpf")
	Pessoa findByCPF(@Param("cpf") String cpf);

	@Query("SELECT obj FROM PessoaJuridica obj WHERE obj.cnpj =:cnpj")
	Pessoa findByCNPJ(@Param("cnpj") String cnpj);

	@EntityGraph(attributePaths = {"enderecos", "enderecos.cidade"})
	@Query("SELECT DISTINCT p FROM Pessoa p")
	List<Pessoa> findAllWithEnderecosAndCidades();
}
