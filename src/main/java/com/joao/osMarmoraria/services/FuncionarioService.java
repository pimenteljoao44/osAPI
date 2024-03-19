package com.joao.osMarmoraria.services;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.joao.osMarmoraria.domain.*;
import com.joao.osMarmoraria.domain.enums.TipoPessoa;
import com.joao.osMarmoraria.dtos.ClienteDTO;
import com.joao.osMarmoraria.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.joao.osMarmoraria.dtos.FuncionarioDTO;
import com.joao.osMarmoraria.repository.FuncionarioRepository;
import com.joao.osMarmoraria.repository.PessoaRepository;
import com.joao.osMarmoraria.services.exceptions.DataIntegratyViolationException;
import com.joao.osMarmoraria.services.exceptions.ObjectNotFoundException;

import javax.transaction.Transactional;
import javax.validation.Valid;

@Service
public class FuncionarioService {

	@Autowired
	private FuncionarioRepository funcionarioRepository;

	@Autowired
	private PessoaRepository pessoaRepository;

	public Funcionario findById(Integer id) {
		Optional<Funcionario> obj = funcionarioRepository.findById(id);
		return obj.orElseThrow(() -> new ObjectNotFoundException(
				"Objeto não encontrado! id:" + id + ",tipo: " + Cliente.class.getName()));
	}

	public List<Funcionario> findAll() {
		return funcionarioRepository.findAll();
	}

	@Transactional
	public Funcionario create(FuncionarioDTO objDTO) {
		if (findByDocumento(objDTO) != null) {
			throw new DataIntegratyViolationException("Documento já cadastrado na base de dados!");
		}

		Pessoa pessoa = createPessoaFromDTO(objDTO);
		Funcionario funcionario  = new Funcionario();
		funcionario.setDataCriacao(new Date());
		funcionario.setPessoa(pessoa);

		return funcionarioRepository.save(funcionario);
	}

	@Transactional
	public Funcionario update(Integer id, @Valid FuncionarioDTO objDTO) {
		Funcionario oldObj = findById(id);
		if (findByDocumento(objDTO) != null && findByDocumento(objDTO).getId() != id) {
			throw new DataIntegratyViolationException("Documento já cadastrado na base de dados!");
		}

		oldObj.getPessoa().setNome(objDTO.getNome());
		oldObj.getPessoa().setTelefone(objDTO.getTelefone());
		oldObj.setDataAtualizacao(new Date());

		if (oldObj.getPessoa() instanceof PessoaFisica) {
			((PessoaFisica) oldObj.getPessoa()).setCpf(objDTO.getCpf());
			((PessoaFisica) oldObj.getPessoa()).setRg(objDTO.getRg());
		} else if (oldObj.getPessoa() instanceof PessoaJuridica) {
			((PessoaJuridica) oldObj.getPessoa()).setCnpj(objDTO.getCnpj());
		}

		return funcionarioRepository.save(oldObj);
	}

	@Transactional
	public void delete(Integer id) {
		Funcionario obj = findById(id);
		if (obj.getListOs().size() > 0) {
			throw new DataIntegratyViolationException("A Pessoa possui ordens de serviço, não pode ser deletada!");
		}
		funcionarioRepository.deleteById(id);
	}

	private Pessoa createPessoaFromDTO(FuncionarioDTO objDTO) {
		if (objDTO.getTipoPessoa() == TipoPessoa.PESSOA_FISICA) {
			return new PessoaFisica(objDTO.getId(), objDTO.getNome(), objDTO.getEndereco(), objDTO.getTelefone(), objDTO.getCpf(), objDTO.getRg());
		} else {
			return new PessoaJuridica(objDTO.getId(), objDTO.getNome(), objDTO.getEndereco(), objDTO.getTelefone(), objDTO.getCnpj());
		}
	}

	private Pessoa findByDocumento(FuncionarioDTO objDTO) {
		if (objDTO.getTipoPessoa() == TipoPessoa.PESSOA_FISICA) {
			return pessoaRepository.findByCPF(objDTO.getCpf());
		} else if (objDTO.getTipoPessoa() == TipoPessoa.PESSOA_JURIDICA) {
			return pessoaRepository.findByCNPJ(objDTO.getCnpj());
		}

		return null;
	}
}