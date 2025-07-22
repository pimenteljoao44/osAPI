package com.joao.osMarmoraria.dtos;

import com.joao.osMarmoraria.domain.Produto;

import java.math.BigDecimal;

public class MaterialSugeridoDTO {

    private Integer produtoId;

    private Produto produto;

    private BigDecimal quantidadeRecomendada;

    private String aplicacao;

    // Construtores
    public MaterialSugeridoDTO() {
    }

    public MaterialSugeridoDTO(Integer produtoId, Produto produto, BigDecimal quantidadeRecomendada, String aplicacao) {
        this.produtoId = produtoId;
        this.produto = produto;
        this.quantidadeRecomendada = quantidadeRecomendada;
        this.aplicacao = aplicacao;
    }

    // Getters e Setters
    public Integer getProdutoId() {
        return produtoId;
    }

    public void setProdutoId(Integer produtoId) {
        this.produtoId = produtoId;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public BigDecimal getQuantidadeRecomendada() {
        return quantidadeRecomendada;
    }

    public void setQuantidadeRecomendada(BigDecimal quantidadeRecomendada) {
        this.quantidadeRecomendada = quantidadeRecomendada;
    }

    public String getAplicacao() {
        return aplicacao;
    }

    public void setAplicacao(String aplicacao) {
        this.aplicacao = aplicacao;
    }
}
