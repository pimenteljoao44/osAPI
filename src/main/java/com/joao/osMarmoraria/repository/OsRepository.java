package com.joao.osMarmoraria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.joao.osMarmoraria.domain.Funcionario;
import com.joao.osMarmoraria.domain.OrdemDeServico;

@Repository
public interface OsRepository extends JpaRepository<OrdemDeServico,Integer> {

}
