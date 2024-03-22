package com.joao.osMarmoraria.services;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.joao.osMarmoraria.domain.*;
import com.joao.osMarmoraria.domain.enums.TipoPessoa;
import com.joao.osMarmoraria.dtos.ClienteDTO;
import com.joao.osMarmoraria.repository.ClienteRepository;
import org.hibernate.Hibernate;
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
			throw new DataIntegratyViolationException("Pessoa já cadastrada na base de dados!");
		}

		Pessoa pessoa = createPessoaFromDTO(objDTO);
		pessoa = pessoaRepository.save(pessoa);

		Funcionario funcionario = new Funcionario();
		funcionario.setSalario(objDTO.getSalario());
		funcionario.setCargo(objDTO.getCargo());
		funcionario.setDataCriacao(new Date());
		funcionario.setPessoa(pessoa);

		return funcionarioRepository.save(funcionario);
	}

	@Transactional
	public Funcionario update(Integer id, @Valid FuncionarioDTO objDTO) {
		Funcionario oldObj = findById(id);

		boolean tipoPessoaAlterado = oldObj.getPessoa() instanceof PessoaFisica && objDTO.getTipoPessoa() == TipoPessoa.PESSOA_JURIDICA ||
				oldObj.getPessoa() instanceof PessoaJuridica && objDTO.getTipoPessoa() == TipoPessoa.PESSOA_FISICA;

		if (tipoPessoaAlterado) {
			Pessoa novaPessoa = createPessoaFromDTO(objDTO);

			novaPessoa.setId(oldObj.getPessoa().getId());

			oldObj.setPessoa(novaPessoa);
		} else {
			oldObj.getPessoa().setNome(objDTO.getNome());
			oldObj.getPessoa().setTelefone(objDTO.getTelefone());
			oldObj.setCargo(objDTO.getCargo());
			oldObj.setSalario(objDTO.getSalario());
			oldObj.setCtps(objDTO.getCtps());
			if (objDTO.getTipoPessoa() == TipoPessoa.PESSOA_FISICA) {
				((PessoaFisica) oldObj.getPessoa()).setCpf(objDTO.getCpf());
				((PessoaFisica) oldObj.getPessoa()).setRg(objDTO.getRg());
			} else {
				((PessoaJuridica) oldObj.getPessoa()).setCnpj(objDTO.getCnpj());
			}

			if (objDTO.getEndereco() != null) {
				Endereco endereco = oldObj.getPessoa().getEndereco();
				endereco.setRua(objDTO.getEndereco().getRua());
				endereco.setNumero(objDTO.getEndereco().getNumero());
				endereco.setComplemento(objDTO.getEndereco().getComplemento());
				endereco.setBairro(objDTO.getEndereco().getBairro());
				endereco.setCidade(objDTO.getEndereco().getCidade());
				oldObj.getPessoa().setEndereco(endereco);
			}
		}

		oldObj.setDataAtualizacao(new Date());

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
		Pessoa pessoa;
		if (objDTO.getTipoPessoa() == TipoPessoa.PESSOA_FISICA) {
			pessoa = new PessoaFisica();
			((PessoaFisica) pessoa).setCpf(objDTO.getCpf());
			((PessoaFisica) pessoa).setRg(objDTO.getRg());
		} else {
			pessoa = new PessoaJuridica();
			((PessoaJuridica) pessoa).setCnpj(objDTO.getCnpj());
		}
		pessoa.setNome(objDTO.getNome());
		pessoa.setTelefone(objDTO.getTelefone());

		Hibernate.initialize(objDTO.getEndereco().getCidade());

		Endereco endereco = new Endereco();
		endereco.setRua(objDTO.getEndereco().getRua());
		endereco.setNumero(objDTO.getEndereco().getNumero());
		endereco.setComplemento(objDTO.getEndereco().getComplemento());
		endereco.setBairro(objDTO.getEndereco().getBairro());
		endereco.setCidade(objDTO.getEndereco().getCidade());
		endereco.setPessoa(pessoa);

		pessoa.setEndereco(endereco);

		return pessoa;
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