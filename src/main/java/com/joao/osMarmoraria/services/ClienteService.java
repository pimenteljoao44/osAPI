package com.joao.osMarmoraria.services;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.joao.osMarmoraria.domain.*;
import com.joao.osMarmoraria.domain.enums.TipoPessoa;
import com.joao.osMarmoraria.repository.CidadeRepository;
import com.joao.osMarmoraria.repository.EnderecoRepository;
import com.joao.osMarmoraria.repository.ClienteRepository;
import com.joao.osMarmoraria.repository.PessoaRepository;
import com.joao.osMarmoraria.dtos.ClienteDTO;
import com.joao.osMarmoraria.services.exceptions.DataIntegratyViolationException;
import com.joao.osMarmoraria.services.exceptions.ObjectNotFoundException;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;
import javax.validation.Valid;

@Service
public class ClienteService {

	@Autowired
	private ClienteRepository clienteRepository;

	@Autowired
	private PessoaRepository pessoaRepository;

	@Autowired
	private CidadeService cidadeService;

	@Autowired
	private EstadoService estadoService;

	public List<Cliente> findAll() {
		return clienteRepository.findAll();
	}

	public Cliente findById(Integer id) {
		Optional<Cliente> obj = clienteRepository.findById(id);
		return obj.orElseThrow(() -> new ObjectNotFoundException(
				"Objeto não encontrado! id:" + id + ",tipo: " + Cliente.class.getName()));
	}

	@Transactional
	public Cliente create(ClienteDTO objDTO) {
		if (findByDocumento(objDTO) != null) {
			throw new DataIntegratyViolationException("Cliente já cadastrado na base de dados!");
		}

		Pessoa pessoa = createPessoaFromDTO(objDTO);
		pessoa = pessoaRepository.save(pessoa);

		Cliente cliente = new Cliente();
		cliente.setDataCriacao(new Date());
		cliente.setPessoa(pessoa);

		return clienteRepository.save(cliente);
	}

	@Transactional
	public Cliente update(Integer id, @Valid ClienteDTO objDTO) {
		Cliente oldObj = findById(id);

		oldObj.getPessoa().setNome(objDTO.getNome());
		oldObj.getPessoa().setTelefone(objDTO.getTelefone());
		oldObj.setDataAtualizacao(new Date());

		if (objDTO.getEndereco() != null) {
			Endereco endereco = oldObj.getPessoa().getEndereco();
			endereco.setRua(objDTO.getEndereco().getRua());
			endereco.setNumero(objDTO.getEndereco().getNumero());
			endereco.setComplemento(objDTO.getEndereco().getComplemento());
			endereco.setBairro(objDTO.getEndereco().getBairro());
			endereco.setCidade(objDTO.getEndereco().getCidade());
			oldObj.getPessoa().setEndereco(endereco);
		}

		if (oldObj.getPessoa() instanceof PessoaFisica) {
			((PessoaFisica) oldObj.getPessoa()).setCpf(objDTO.getCpf());
			((PessoaFisica) oldObj.getPessoa()).setRg(objDTO.getRg());
		}
		else if (oldObj.getPessoa() instanceof PessoaJuridica) {
			((PessoaJuridica) oldObj.getPessoa()).setCnpj(objDTO.getCnpj());
		}

		return clienteRepository.save(oldObj);
	}

	@Transactional
	public void delete(Integer id) {
		Cliente cliente = clienteRepository.findById(id)
				.orElseThrow(() -> new ObjectNotFoundException("Cliente não encontrado! Id: " + id));

		if (!cliente.getListOs().isEmpty()) {
			throw new DataIntegratyViolationException("O Cliente possui ordens de serviço, não pode ser deletado!");
		}

		clienteRepository.delete(cliente);
	}

	private Pessoa createPessoaFromDTO(ClienteDTO objDTO) {
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

	private Pessoa findByDocumento(ClienteDTO objDTO) {
		if (objDTO.getTipoPessoa() == TipoPessoa.PESSOA_FISICA) {
			return pessoaRepository.findByCPF(objDTO.getCpf());
		} else if (objDTO.getTipoPessoa() == TipoPessoa.PESSOA_JURIDICA) {
			return pessoaRepository.findByCNPJ(objDTO.getCnpj());
		}

		return null;
	}
}