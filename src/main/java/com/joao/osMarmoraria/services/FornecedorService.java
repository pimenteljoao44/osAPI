package com.joao.osMarmoraria.services;

import com.joao.osMarmoraria.domain.*;
import com.joao.osMarmoraria.domain.enums.TipoPessoa;
import com.joao.osMarmoraria.dtos.*;
import com.joao.osMarmoraria.repository.CidadeRepository;
import com.joao.osMarmoraria.repository.EstadoRepository;
import com.joao.osMarmoraria.repository.FornecedorRepository;
import com.joao.osMarmoraria.repository.PessoaRepository;
import com.joao.osMarmoraria.services.exceptions.DataIntegratyViolationException;
import com.joao.osMarmoraria.services.exceptions.ObjectNotFoundException;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class FornecedorService {
    @Autowired
    private FornecedorRepository fornecedorRepository;
    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private EstadoRepository estadoRepository;

    @Autowired
    private CidadeRepository cidadeRepository;

    @Autowired
    private EstadoService estadoService;
    public List<Fornecedor> findAll(){return fornecedorRepository.findAll();}

    public Fornecedor findById(Integer id){
        Optional<Fornecedor> obj = fornecedorRepository.findById(id);
        return obj.orElseThrow(() -> new ObjectNotFoundException(
                "Objeto não encontrado! id:" + id + ",tipo: " + Fornecedor.class.getName()));
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
    public Fornecedor create(FornecedorDTO objDTO) {
        if (findByDocumento(objDTO) != null) {
            throw new DataIntegratyViolationException("Fornecedor já cadastrado na base de dados!");
        }

        Pessoa pessoa = createPessoaFromDTO(objDTO);
        pessoa = pessoaRepository.save(pessoa);

        Fornecedor f = new Fornecedor();
        f.setDataCriacao(new Date());
        f.setPessoa(pessoa);

        return fornecedorRepository.save(f);
    }

    @Transactional
    public Fornecedor update(Integer id, @Valid FornecedorDTO objDTO) {
        Fornecedor oldObj = findById(id);

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
        }
        return fornecedorRepository.save(oldObj);
    }

    @Transactional
    public void delete(Integer id) {
        Fornecedor fornecedor = fornecedorRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Fornecedor não encontrado! Id: " + id));

        fornecedorRepository.delete(fornecedor);
    }

    private Pessoa createPessoaFromDTO(FornecedorDTO objDTO) {
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

        return pessoa;
    }

    private Pessoa findByDocumento(FornecedorDTO objDTO) {
        if (objDTO.getTipoPessoa() == TipoPessoa.PESSOA_FISICA) {
            return pessoaRepository.findByCPF(objDTO.getCpf());
        } else if (objDTO.getTipoPessoa() == TipoPessoa.PESSOA_JURIDICA) {
            return pessoaRepository.findByCNPJ(objDTO.getCnpj());
        }
        return null;
    }
}
