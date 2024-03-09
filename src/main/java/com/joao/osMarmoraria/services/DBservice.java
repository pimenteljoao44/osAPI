package com.joao.osMarmoraria.services;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.joao.osMarmoraria.domain.Cliente;
import com.joao.osMarmoraria.domain.Funcionario;
import com.joao.osMarmoraria.domain.OrdemDeServico;
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
		Funcionario f1 = new Funcionario(null,"João","921.772.070-45","(44)99963-2007");
		Cliente c1 = new Cliente(null,"betina","573.334.940-98","(44)99963-8888");
		OrdemDeServico os1 = new OrdemDeServico(null,Prioridade.ALTA, "TESTE",Status.ABERTO,f1,c1);
		
		f1.getListOs().add(os1);
		c1.getListOs().add(os1);
		
		funcionarioRepository.saveAll(Arrays.asList(f1));
		clienteRepository.saveAll(Arrays.asList(c1));
		osRepository.saveAll(Arrays.asList(os1));
	}

}
