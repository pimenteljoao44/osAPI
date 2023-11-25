package com.joao.osMarmoraria.services;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.joao.osMarmoraria.domain.Cliente;
import com.joao.osMarmoraria.domain.Funcionario;
import com.joao.osMarmoraria.domain.OrdemDeServico;
import com.joao.osMarmoraria.domain.enums.Prioridade;
import com.joao.osMarmoraria.domain.enums.Status;
import com.joao.osMarmoraria.dtos.OsDTO;
import com.joao.osMarmoraria.repository.OsRepository;
import com.joao.osMarmoraria.services.exceptions.ObjectNotFoundException;

import jakarta.validation.Valid;

@Service
public class OsService {

	@Autowired
	private OsRepository osrepositoy;
	
	@Autowired
	private FuncionarioService funcionarioService;
	
	@Autowired
	private ClienteService clienteService;
	
	public OrdemDeServico findById(Integer id) {
		Optional<OrdemDeServico> obj = osrepositoy.findById(id);
		return obj.orElseThrow(() -> new ObjectNotFoundException("Objeto não encontrado id:"+ id + " tipo:"+
		OrdemDeServico.class.getName()));
	}
	
	public List<OrdemDeServico> findAll(){
		return osrepositoy.findAll();
	}

	public OrdemDeServico create(@Valid OsDTO obj) {
		return fromDTO(obj);
	}
	
	public OrdemDeServico update(@Valid OsDTO obj) {
		findById(obj.getId());
		return fromDTO(obj);
	}
	
	private OrdemDeServico fromDTO(OsDTO obj) {
		OrdemDeServico newObj = new OrdemDeServico();
		newObj.setId(obj.getId());
		newObj.setObservacoes(obj.getObservacoes());
		newObj.setPrioridade(Prioridade.toEnum(obj.getPrioridade()));
		newObj.setStatus(Status.toEnum(obj.getStatus()));
		
		Funcionario f = funcionarioService.findById(obj.getFuncionario());
		Cliente cli = clienteService.findById(obj.getCliente());
		
		newObj.setCliente(cli);
		newObj.setFuncionario(f);
		
		if(newObj.getStatus().getCod().equals(2)) {
			newObj.setDataFechamento(LocalDateTime.now());
		}
		return osrepositoy.save(newObj);
	}
}
