package com.joao.osMarmoraria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.joao.osMarmoraria.domain.Funcionario;

@Repository
public interface FuncionarioRepository extends JpaRepository<Funcionario,Integer> {

	@Query("SELECT obj FROM Funcionario obj WHERE obj.cpf =:cpf")
	Funcionario findByCPF(@Param("cpf") String cpf);

}
