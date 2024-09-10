package com.joao.osMarmoraria.dtos;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.joao.osMarmoraria.domain.Fornecedor;
import com.joao.osMarmoraria.domain.Grupo;
import com.joao.osMarmoraria.domain.OrdemDeServico;
import com.joao.osMarmoraria.domain.Produto;

import javax.persistence.CascadeType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.io.Serializable;
import java.math.BigDecimal;

public class ProdutoDTO implements Serializable {
    private Integer id;
    private String nome;
    private BigDecimal preco;
    private Boolean ativo;
    private BigDecimal estoque;
    private BigDecimal quantidade;
    @JsonProperty("grupo")
    private Grupo grupo;
    private Integer fornecedor;
    private Integer ordemDeServico;

    public ProdutoDTO(Produto obj) {
        this.id = obj.getProdId();
        this.nome = obj.getNome();
        this.preco = obj.getPrecoCusto();
        this.ativo = obj.getAtivo();
        this.estoque = obj.getEstoque();
        this.quantidade = obj.getQuantidade();
        this.grupo = obj.getGrupo();

        if (obj.getFornecedor() != null) {
            this.fornecedor = obj.getFornecedor().getId();
        }

        if (obj.getOrdemDeServico() != null) {
            this.ordemDeServico = obj.getOrdemDeServico().getId();
        }
    }


    public ProdutoDTO() {
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

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public BigDecimal getEstoque() {
        return estoque;
    }

    public void setEstoque(BigDecimal estoque) {
        this.estoque = estoque;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(BigDecimal quantidade) {
        this.quantidade = quantidade;
    }

    public Grupo getGrupo() {
        return grupo;
    }

    public void setGrupo(Grupo grupo) {
        this.grupo = grupo;
    }

    public Integer getFornecedor() {
        return fornecedor;
    }

    public void setFornecedor(Integer fornecedor) {
        this.fornecedor = fornecedor;
    }

    public Integer getOrdemDeServico() {
        return ordemDeServico;
    }

    public void setOrdemDeServico(Integer ordemDeServico) {
        this.ordemDeServico = ordemDeServico;
    }
}
