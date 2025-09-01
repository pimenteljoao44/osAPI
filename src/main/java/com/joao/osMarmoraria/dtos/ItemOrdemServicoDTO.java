package com.joao.osMarmoraria.dtos;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.joao.osMarmoraria.domain.Produto;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

public class ItemOrdemServicoDTO {

    private Integer id;

    private Integer ordemServicoId;

    @JsonBackReference("os-items")
    private OrdemServicoDTO ordemServico;

    @NotNull(message = "Produto é obrigatório")
    private Integer produtoId;

    private Produto produto;

    @NotNull(message = "Quantidade é obrigatória")
    @DecimalMin(value = "0.001", message = "Quantidade deve ser maior que zero")
    private BigDecimal quantidade;

    @NotNull(message = "Valor unitário é obrigatório")
    @DecimalMin(value = "0.0", inclusive = false, message = "Valor unitário deve ser maior que zero")
    private BigDecimal valorUnitario;

    @NotNull(message = "Valor total é obrigatório")
    @DecimalMin(value = "0.0", inclusive = false, message = "Valor total deve ser maior que zero")
    private BigDecimal valorTotal;

    @Size(max = 500, message = "Observações devem ter no máximo 500 caracteres")
    private String observacoes;

    // Construtores
    public ItemOrdemServicoDTO() {
    }

    public ItemOrdemServicoDTO(Integer produtoId, BigDecimal quantidade, BigDecimal valorUnitario) {
        this.produtoId = produtoId;
        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario;
        this.valorTotal = quantidade.multiply(valorUnitario);
    }

    // Getters e Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getOrdemServicoId() {
        return ordemServicoId;
    }

    public void setOrdemServicoId(Integer ordemServicoId) {
        this.ordemServicoId = ordemServicoId;
    }

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

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(BigDecimal quantidade) {
        this.quantidade = quantidade;
        if (this.valorUnitario != null) {
            this.valorTotal = quantidade.multiply(this.valorUnitario);
        }
    }

    public BigDecimal getValorUnitario() {
        return valorUnitario;
    }

    public void setValorUnitario(BigDecimal valorUnitario) {
        this.valorUnitario = valorUnitario;
        if (this.quantidade != null) {
            this.valorTotal = this.quantidade.multiply(valorUnitario);
        }
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}

