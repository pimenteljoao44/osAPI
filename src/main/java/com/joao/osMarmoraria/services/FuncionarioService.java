package com.joao.osMarmoraria.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.joao.osMarmoraria.domain.Funcionario;
import com.joao.osMarmoraria.domain.Pessoa;
import com.joao.osMarmoraria.dtos.FuncionarioDTO;
import com.joao.osMarmoraria.repository.FuncionarioRepository;
import com.joao.osMarmoraria.repository.PessoaRepository;
import com.joao.osMarmoraria.services.exceptions.DataIntegratyViolationException;
import com.joao.osMarmoraria.services.exceptions.ObjectNotFoundException;

import javax.validation.Valid;

@Service
public class FuncionarioService {

	@Autowired
	private FuncionarioRepository funcionarioRepository;
	
	@Autowired
	private PessoaRepository pessoaRepository;
	
	public Funcionario findById(Integer id) {
		Optional<Funcionario> obj = funcionarioRepository.findById(id);
		return obj.orElseThrow(()-> new ObjectNotFoundException(
				"Objeto não encontrado! id:"+id+",tipo: "+Funcionario.class.getName()));
	}

	public List<Funcionario> findAll() {
		return funcionarioRepository.findAll();
	}
	
	public Funcionario create(FuncionarioDTO objDTO) {
		if(findByCPF(objDTO) != null) {
			throw new DataIntegratyViolationException("CPF já cadastrado na base de dados!");
		}
		Funcionario newObj = new Funcionario(null,objDTO.getNome(),objDTO.getCpf(),objDTO.getTelefone());
		return funcionarioRepository.save(newObj);
	}
	
	public Funcionario update(Integer id, @Valid FuncionarioDTO objDTO) {
		Funcionario oldObj = findById(id);
		if(findByCPF(objDTO) != null && findByCPF(objDTO).getId() != id) {
			throw new DataIntegratyViolationException("CPF já cadastrado na base de dados!");
		}
		oldObj.setNome(objDTO.getNome());
		oldObj.setCpf(objDTO.getCpf());
		oldObj.setTelefone(objDTO.getTelefone());
		return funcionarioRepository.save(oldObj);
	}
	
	public void delete(Integer id) {
	  Funcionario obj =	findById(id);
	  if(obj.getListOs().size() > 0) {
		  throw new DataIntegratyViolationException("O Funcionario possui ordens de serviço, não pode ser deletado!");
	  }
      funcionarioRepository.deleteById(id);	
	}
	
	private Pessoa findByCPF(FuncionarioDTO objDTO) {
		Pessoa obj = pessoaRepository.findByCPF(objDTO.getCpf());
		if(obj != null) {
			return obj;
		}
		return null;
	}
}
