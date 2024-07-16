package com.joao.osMarmoraria.services;

import com.joao.osMarmoraria.domain.*;
import com.joao.osMarmoraria.domain.enums.TipoPessoa;
import com.joao.osMarmoraria.dtos.ClienteDTO;
import com.joao.osMarmoraria.dtos.FornecedorDTO;
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
    private CidadeService cidadeService;

    @Autowired
    private EstadoService estadoService;
    public List<Fornecedor> findAll(){return fornecedorRepository.findAll();}

    public Fornecedor findById(Integer id){
        Optional<Fornecedor> obj = fornecedorRepository.findById(id);
        return obj.orElseThrow(() -> new ObjectNotFoundException(
                "Objeto não encontrado! id:" + id + ",tipo: " + Fornecedor.class.getName()));
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

        boolean tipoPessoaAlterado = oldObj.getPessoa() instanceof PessoaFisica && objDTO.getTipoPessoa() == TipoPessoa.PESSOA_JURIDICA ||
                oldObj.getPessoa() instanceof PessoaJuridica && objDTO.getTipoPessoa() == TipoPessoa.PESSOA_FISICA;

        if (tipoPessoaAlterado) {
            Pessoa novaPessoa = createPessoaFromDTO(objDTO);

            novaPessoa.setId(oldObj.getPessoa().getId());

            oldObj.setPessoa(novaPessoa);
        } else {
            oldObj.getPessoa().setNome(objDTO.getNome());
            oldObj.getPessoa().setTelefone(objDTO.getTelefone());

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

        return fornecedorRepository.save(oldObj);
    }

    @Transactional
    public void delete(Integer id) {
        Fornecedor fornecedor = fornecedorRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Fornecedor não encontrado! Id: " + id));

        fornecedorRepository.delete(fornecedor);
    }

    private Pessoa createPessoaFromDTO(FornecedorDTO objDTO) {
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

    private Pessoa findByDocumento(FornecedorDTO objDTO) {
        if (objDTO.getTipoPessoa() == TipoPessoa.PESSOA_FISICA) {
            return pessoaRepository.findByCPF(objDTO.getCpf());
        } else if (objDTO.getTipoPessoa() == TipoPessoa.PESSOA_JURIDICA) {
            return pessoaRepository.findByCNPJ(objDTO.getCnpj());
        }
        return null;
    }
}
