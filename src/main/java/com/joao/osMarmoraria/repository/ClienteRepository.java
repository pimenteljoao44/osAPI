package com.joao.osMarmoraria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.joao.osMarmoraria.domain.Cliente;
import com.joao.osMarmoraria.domain.Funcionario;
import com.joao.osMarmoraria.domain.OrdemDeServico;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente,Integer> {

}
