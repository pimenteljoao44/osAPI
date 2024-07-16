package com.joao.osMarmoraria.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.joao.osMarmoraria.domain.*;
import com.joao.osMarmoraria.domain.enums.TipoPessoa;
import org.hibernate.validator.constraints.br.CNPJ;
import org.hibernate.validator.constraints.br.CPF;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class FornecedorDTO implements Serializable {

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
    @NotEmpty(message = "A venda deve obrigatóriamente conter produtos!")
    private List<Integer> produtos;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm", timezone = "America/Sao_Paulo")
    private Date dataCriacao;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm", timezone = "America/Sao_Paulo")
    private Date dataAtualizacao;

    public FornecedorDTO(Fornecedor obj) {
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

    public @NotEmpty(message = "O campo Nome é requerido!") String getNome() {
        return nome;
    }

    public void setNome(@NotEmpty(message = "O campo Nome é requerido!") String nome) {
        this.nome = nome;
    }

    public @NotEmpty(message = "O campo Telefone é requerido!") String getTelefone() {
        return telefone;
    }

    public void setTelefone(@NotEmpty(message = "O campo Telefone é requerido!") String telefone) {
        this.telefone = telefone;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }

    public @NotEmpty(message = "O campo CPF é requerido!", groups = PessoaFisica.class) @CPF(message = "O campo CPF é inválido", groups = PessoaFisica.class) String getCpf() {
        return cpf;
    }

    public void setCpf(@NotEmpty(message = "O campo CPF é requerido!", groups = PessoaFisica.class) @CPF(message = "O campo CPF é inválido", groups = PessoaFisica.class) String cpf) {
        this.cpf = cpf;
    }

    public String getRg() {
        return rg;
    }

    public void setRg(String rg) {
        this.rg = rg;
    }

    public @NotEmpty(message = "O campo CNPJ é requerido para pessoa jurídica!", groups = PessoaJuridica.class) @CNPJ(message = "O campo CNPJ é inválido", groups = PessoaJuridica.class) String getCnpj() {
        return cnpj;
    }

    public void setCnpj(@NotEmpty(message = "O campo CNPJ é requerido para pessoa jurídica!", groups = PessoaJuridica.class) @CNPJ(message = "O campo CNPJ é inválido", groups = PessoaJuridica.class) String cnpj) {
        this.cnpj = cnpj;
    }

    public @NotNull(message = "o campo tipo de pessoa é requerido!") TipoPessoa getTipoPessoa() {
        return tipoPessoa;
    }

    public void setTipoPessoa(@NotNull(message = "o campo tipo de pessoa é requerido!") TipoPessoa tipoPessoa) {
        this.tipoPessoa = tipoPessoa;
    }

    public @NotEmpty(message = "A venda deve obrigatóriamente conter produtos!") List<Integer> getProdutos() {
        return produtos;
    }

    public void setProdutos(@NotEmpty(message = "A venda deve obrigatóriamente conter produtos!") List<Integer> produtos) {
        this.produtos = produtos;
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
