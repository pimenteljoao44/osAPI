package com.joao.osMarmoraria.services;

import com.joao.osMarmoraria.domain.Cliente;
import com.joao.osMarmoraria.domain.Endereco;
import com.joao.osMarmoraria.domain.Pessoa;
import com.joao.osMarmoraria.dtos.PessoaDTO;
import com.joao.osMarmoraria.repository.EnderecoRepository;
import com.joao.osMarmoraria.repository.PessoaRepository;
import com.joao.osMarmoraria.services.exceptions.DataIntegratyViolationException;
import com.joao.osMarmoraria.services.exceptions.ObjectNotFoundException;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class PessoaService {

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private EnderecoRepository enderecoRepository;

    public List<Pessoa> findAll() {
        return pessoaRepository.findAll();
    }

    public Pessoa findById(Integer id) {
        Optional<Pessoa> obj = pessoaRepository.findById(id);
        return obj.orElseThrow(() -> new ObjectNotFoundException(
                "Objeto não encontrado! id:" + id + ",tipo: " + Pessoa.class.getName()));
    }

    public Pessoa create(PessoaDTO objDTO) {
        if (findById(objDTO.getId()) != null) {
            throw new DataIntegratyViolationException("Pessoa já cadastrada na base de dados!");
        }
        Pessoa pessoa = new Pessoa();
        pessoa.setNome(objDTO.getNome());
        pessoa.setTelefone(objDTO.getTelefone());

        Endereco endereco = objDTO.getEndereco();
        if (endereco != null) {
            endereco.setPessoa(pessoa);
            endereco.setDataCriacao(new Date());
            endereco.setDataAtualizacao(new Date());
            pessoa.setEndereco(endereco);
        }

        return pessoaRepository.save(pessoa);
    }

    public Pessoa update(Integer id, @Valid PessoaDTO objDTO) {
        Pessoa pessoa = findById(id);
        pessoa.setNome(objDTO.getNome());
        pessoa.setTelefone(objDTO.getTelefone());

        Endereco endereco = objDTO.getEndereco();
        if (endereco != null) {
            if (endereco.getEnderecoId() != null) {
                Endereco existingEndereco = enderecoRepository.getById(endereco.getEnderecoId());
                endereco.setDataCriacao(existingEndereco.getDataCriacao());
            }

            endereco.setPessoa(pessoa);
            endereco.setDataAtualizacao(new Date());
            pessoa.setEndereco(endereco);
        }
        return pessoaRepository.save(pessoa);
    }

    public void delete(Integer id) {
        pessoaRepository.deleteById(id);
    }
}
