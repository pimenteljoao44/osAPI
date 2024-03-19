package com.joao.osMarmoraria.dtos;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.joao.osMarmoraria.domain.*;
import com.joao.osMarmoraria.domain.enums.TipoPessoa;
import org.hibernate.validator.constraints.br.CNPJ;
import org.hibernate.validator.constraints.br.CPF;

import javax.validation.constraints.NotEmpty;

public class FuncionarioDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer id;
    @NotEmpty(message = "O campo Nome é requerido!")
    private String nome;
    @CPF
    @NotEmpty(message = "O campo CPF é requerido!")
    private String cpf;

    @NotEmpty(message = "O campo RG é requerido!")
    private String rg;
    @NotEmpty(message = "O campo Telefone é requerido!")
    private String telefone;
    @NotEmpty(message = "O campo CNPJ é requerido!")
    @CNPJ
    private String cnpj;
    @NotEmpty(message = "o campo tipo de pessoa é requerido!")
    private TipoPessoa tipoPessoa;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    private Date dataCriacao;
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    private Date dataAtualizacao;

    private Endereco endereco;

    public FuncionarioDTO() {
        super();
    }

    public FuncionarioDTO(Funcionario obj) {
        this.id = obj.getId();
        this.nome = obj.getPessoa().getNome();
        this.telefone = obj.getPessoa().getTelefone();
        this.endereco = obj.getPessoa().getEndereco();
        Pessoa pessoa = obj.getPessoa();
        if (pessoa instanceof PessoaFisica) {
            PessoaFisica pessoaFisica = (PessoaFisica) pessoa;
            this.cpf = pessoaFisica.getCpf();
            this.rg = pessoaFisica.getRg();
            this.tipoPessoa = TipoPessoa.PESSOA_FISICA;
        } else if (pessoa instanceof PessoaJuridica) {
            PessoaJuridica pessoaJuridica = (PessoaJuridica) pessoa;
            this.cnpj = pessoaJuridica.getCnpj();
            this.tipoPessoa = TipoPessoa.PESSOA_JURIDICA;
        }
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

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getRg() {
        return rg;
    }

    public void setRg(String rg) {
        this.rg = rg;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public TipoPessoa getTipoPessoa() {
        return tipoPessoa;
    }

    public void setTipoPessoa(TipoPessoa tipoPessoa) {
        this.tipoPessoa = tipoPessoa;
    }

    public Date getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(Date dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public Date getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(Date dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }
}
