package com.joao.osMarmoraria.dtos;

import com.joao.osMarmoraria.domain.Endereco;
import com.joao.osMarmoraria.domain.Pessoa;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

public class PessoaDTO implements Serializable {

    private Integer id;
    @NotEmpty(message = "O campo Nome é requerido!")
    private String nome;

    private Endereco endereco;
    @NotEmpty(message = "O campo Telefone é requerido!")
    private String telefone;

    public PessoaDTO() {
    }

    public PessoaDTO(Pessoa obj) {
        this.id = obj.getId();
        this.nome = obj.getNome();
        this.endereco = obj.getEndereco();
        this.telefone = obj.getTelefone();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }
}
