package com.joao.osMarmoraria.services;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.joao.osMarmoraria.domain.*;
import com.joao.osMarmoraria.domain.enums.TipoPessoa;
import com.joao.osMarmoraria.dtos.*;
import com.joao.osMarmoraria.repository.*;
import com.joao.osMarmoraria.services.exceptions.DataIntegratyViolationException;
import com.joao.osMarmoraria.services.exceptions.ObjectNotFoundException;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@Service
public class ClienteService {

	@Autowired
	private ClienteRepository clienteRepository;

	@Autowired
	private PessoaRepository pessoaRepository;

	@Autowired
	private CidadeRepository cidadeRepository;

	@Autowired
	private EstadoRepository estadoRepository;

	public List<Cliente> findAll() {
		return clienteRepository.findAll();
	}

	public Cliente findById(Integer id) {
		Optional<Cliente> obj = clienteRepository.findById(id);
		return obj.orElseThrow(() -> new ObjectNotFoundException(
				"Objeto não encontrado! id:" + id + ",tipo: " + Cliente.class.getName()));
	}

	public Cidade handleCidade(CidadeInputDTO cidadeDTO) {
		// Busca por ID
		if (cidadeDTO.getId() != null) {
			Optional<Cidade> cidadeOpt = cidadeRepository.findById(cidadeDTO.getId());
			if (cidadeOpt.isPresent()) {
				return cidadeOpt.get();
			}
		}
		// Busca por nome+UF
		Optional<Cidade> cidadeOpt = cidadeRepository.findByNomeAndUf(
				cidadeDTO.getNome(), cidadeDTO.getUf()
		);
		if (cidadeOpt.isPresent()) {
			return cidadeOpt.get();
		}
		// Se não existe, cria nova
		Cidade novaCidade = new Cidade();
		novaCidade.setId(cidadeDTO.getId());
		novaCidade.setNome(cidadeDTO.getNome());
		novaCidade.setUf(cidadeDTO.getUf());
		return cidadeRepository.save(novaCidade);
	}

	public Estado handleEstado(EstadoInputDTO estadoDTO) {
		if (estadoDTO.getId() != null) {
			Optional<Estado> estadoOpt = estadoRepository.findById(estadoDTO.getId());
			if (estadoOpt.isPresent()) {
				return estadoOpt.get();
			}
		}
		Optional<Estado> estadoOpt = estadoRepository.findByNomeAndSigla(
				estadoDTO.getNome(), estadoDTO.getSigla()
		);
		if (estadoOpt.isPresent()) {
			return estadoOpt.get();
		}
		Estado novoEstado = new Estado();
		novoEstado.setId(estadoDTO.getId());
		novoEstado.setNome(estadoDTO.getNome());
		novoEstado.setSigla(estadoDTO.getSigla());
		return estadoRepository.save(novoEstado);
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

		boolean tipoPessoaAlterado = oldObj.getPessoa() instanceof PessoaFisica && objDTO.getTipoPessoa() == TipoPessoa.PESSOA_JURIDICA
				|| oldObj.getPessoa() instanceof PessoaJuridica && objDTO.getTipoPessoa() == TipoPessoa.PESSOA_FISICA;

		if (tipoPessoaAlterado) {
			Pessoa novaPessoa = createPessoaFromDTO(objDTO);
			novaPessoa.setId(oldObj.getPessoa().getId());

			pessoaRepository.save(novaPessoa);
			oldObj.setPessoa(novaPessoa);
		} else {
			Pessoa pessoa = oldObj.getPessoa();
			pessoa.setNome(objDTO.getNome());
			pessoa.setTelefone(objDTO.getTelefone());

			if (pessoa instanceof PessoaFisica) {
				((PessoaFisica) pessoa).setCpf(objDTO.getCpf());
				((PessoaFisica) pessoa).setRg(objDTO.getRg());
			} else if (pessoa instanceof PessoaJuridica) {
				((PessoaJuridica) pessoa).setCnpj(objDTO.getCnpj());
			}

			if (objDTO.getEndereco() != null) {
				Endereco endereco = pessoa.getEndereco();

				if (endereco == null) {
					endereco = new Endereco();
					endereco.setPessoa(pessoa);
					pessoa.setEndereco(endereco);
				}

				endereco.setRua(objDTO.getEndereco().getRua());
				endereco.setNumero(objDTO.getEndereco().getNumero());
				endereco.setComplemento(objDTO.getEndereco().getComplemento());
				endereco.setBairro(objDTO.getEndereco().getBairro());

				if (objDTO.getEndereco().getCidade() != null) {
					Cidade cidade = handleCidade(objDTO.getEndereco().getCidade());
					endereco.setCidade(cidade);
				}

				if (objDTO.getEndereco().getEstado() != null) {
					Estado estado = handleEstado(objDTO.getEndereco().getEstado());
					endereco.setEstado(estado);
				}
			}

			pessoaRepository.save(pessoa); // Garantir que as alterações na pessoa sejam persistidas
		}

		oldObj.setDataAtualizacao(new Date());
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
        Pessoa pessoa = objDTO.getTipoPessoa() == TipoPessoa.PESSOA_FISICA ?
                new PessoaFisica() : new PessoaJuridica();

        if (pessoa instanceof PessoaFisica) {
            ((PessoaFisica) pessoa).setCpf(objDTO.getCpf());
            ((PessoaFisica) pessoa).setRg(objDTO.getRg());
        } else {
            ((PessoaJuridica) pessoa).setCnpj(objDTO.getCnpj());
        }
        pessoa.setNome(objDTO.getNome());
        pessoa.setTelefone(objDTO.getTelefone());

        if (pessoa instanceof PessoaFisica) {
            ((PessoaFisica) pessoa).setCpf(objDTO.getCpf());
            ((PessoaFisica) pessoa).setRg(objDTO.getRg());
        } else {
            ((PessoaJuridica) pessoa).setCnpj(objDTO.getCnpj());
        }

        if (objDTO.getEndereco() != null) {
            Endereco endereco = new Endereco();
            endereco.setRua(objDTO.getEndereco().getRua());
            endereco.setNumero(objDTO.getEndereco().getNumero());
            endereco.setComplemento(objDTO.getEndereco().getComplemento());
            endereco.setBairro(objDTO.getEndereco().getBairro());


            if (objDTO.getEndereco().getCidade() != null) {
				Cidade cidade = handleCidade(objDTO.getEndereco().getCidade());
                endereco.setCidade(cidade);
            }

            if (objDTO.getEndereco().getEstado() != null) {
                Estado estado = handleEstado(objDTO.getEndereco().getEstado());
                endereco.setEstado(estado);
            }

            endereco.setPessoa(pessoa);
            pessoa.setEndereco(endereco);
        }

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