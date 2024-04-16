package com.joao.osMarmoraria.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.joao.osMarmoraria.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.joao.osMarmoraria.domain.enums.Prioridade;
import com.joao.osMarmoraria.domain.enums.Status;
import com.joao.osMarmoraria.repository.ClienteRepository;
import com.joao.osMarmoraria.repository.FuncionarioRepository;
import com.joao.osMarmoraria.repository.OsRepository;

@Service
public class DBservice {
	@Autowired
	private FuncionarioRepository funcionarioRepository;
	
	@Autowired
	private ClienteRepository clienteRepository;
	
	@Autowired
	OsRepository osRepository;

	public void instanciaDB() {
		Endereco endereco = new Endereco();

		Pessoa pessoa1 = new Pessoa(null, "João", endereco, "(44)99963-2007");
		Funcionario f1 = new Funcionario(null, "Cargo", new BigDecimal("2000.00"), "12345", pessoa1, new ArrayList<>(), new Date(), new Date());

		Pessoa pessoa2 = new Pessoa(null, "Betina", endereco, "(44)99963-8888");
		Cliente c1 = new Cliente(null, new ArrayList<>(), pessoa2, new Date(), new Date());

		OrdemDeServico os1 = new OrdemDeServico(null, Prioridade.ALTA, "TESTE", Status.ABERTO, f1, c1);

		f1.getListOs().add(os1);
		c1.getListOs().add(os1);

		funcionarioRepository.saveAll(Arrays.asList(f1));
		clienteRepository.saveAll(Arrays.asList(c1));
		osRepository.saveAll(Arrays.asList(os1));
	}

}
