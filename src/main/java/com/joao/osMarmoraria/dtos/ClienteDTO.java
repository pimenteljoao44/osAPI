package com.joao.osMarmoraria.dtos;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.joao.osMarmoraria.domain.*;
import com.joao.osMarmoraria.domain.enums.TipoPessoa;
import com.joao.osMarmoraria.domain.PessoaJuridica;
import com.joao.osMarmoraria.domain.interfaces.TipoPessoaValid;
import org.hibernate.validator.constraints.br.CNPJ;
import org.hibernate.validator.constraints.br.CPF;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@TipoPessoaValid
public class ClienteDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;

    @NotEmpty(message = "O campo Nome é requerido!")
    private String nome;

    @NotEmpty(message = "O campo Telefone é requerido!")
    private String telefone;

    private Endereco endereco;
    @NotEmpty(message = "O campo CPF é requerido!",groups = PessoaFisica.class)
    @CPF(message = "O campo CPF é inválido", groups = PessoaFisica.class)
    private String cpf;

    private String rg;
    @NotEmpty(message = "O campo CNPJ é requerido para pessoa jurídica!", groups = PessoaJuridica.class)
    @CNPJ(message = "O campo CNPJ é inválido", groups = PessoaJuridica.class)
    private String cnpj;
    @NotNull(message = "o campo tipo de pessoa é requerido!")
    @Enumerated(EnumType.STRING)
    private TipoPessoa tipoPessoa;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm", timezone = "America/Sao_Paulo")
    private Date dataCriacao;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm", timezone = "America/Sao_Paulo")
    private Date dataAtualizacao;


    public ClienteDTO() {
        super();
    }

    public ClienteDTO(Cliente obj) {
        super();
        this.id = obj.getCliId();
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

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
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
}