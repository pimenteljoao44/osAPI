package com.joao.osMarmoraria.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "projeto_itens")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ProjetoItem implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "projeto_id", nullable = false)
    @NotNull(message = "Projeto é obrigatório")
    private Integer projetoId;

    @JsonBackReference("projeto-itens")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projeto_id", insertable = false, updatable = false)
    private Projeto projeto;

    @Column(name = "produto_id", nullable = false)
    @NotNull(message = "Produto é obrigatório")
    private Integer produtoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", insertable = false, updatable = false)
    private Produto produto;

    @Column(name = "quantidade", precision = 10, scale = 3, nullable = false)
    @NotNull(message = "Quantidade é obrigatória")
    @DecimalMin(value = "0.001", message = "Quantidade deve ser maior que zero")
    private BigDecimal quantidade;

    @Column(name = "valor_unitario", precision = 10, scale = 2, nullable = false)
    @NotNull(message = "Valor unitário é obrigatório")
    @DecimalMin(value = "0.0", inclusive = false, message = "Valor unitário deve ser maior que zero")
    private BigDecimal valorUnitario;

    @Column(name = "valor_total", precision = 10, scale = 2, nullable = false)
    @NotNull(message = "Valor total é obrigatório")
    @DecimalMin(value = "0.0", inclusive = false, message = "Valor total deve ser maior que zero")
    private BigDecimal valorTotal;

    @Column(name = "observacoes", length = 500)
    @Size(max = 500, message = "Observações devem ter no máximo 500 caracteres")
    private String observacoes;

    // Construtores
    public ProjetoItem() {
    }

    public ProjetoItem(Integer projetoId, Integer produtoId, BigDecimal quantidade, BigDecimal valorUnitario) {
        this.projetoId = projetoId;
        this.produtoId = produtoId;
        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario;
        calcularValorTotal();
    }

    // Métodos de negócio
    @PrePersist
    @PreUpdate
    protected void calcularValorTotal() {
        if (quantidade != null && valorUnitario != null) {
            valorTotal = quantidade.multiply(valorUnitario);
        }
    }

    public void atualizarQuantidade(BigDecimal novaQuantidade) {
        if (novaQuantidade != null && novaQuantidade.compareTo(BigDecimal.ZERO) > 0) {
            this.quantidade = novaQuantidade;
            calcularValorTotal();
        }
    }

    public void atualizarValorUnitario(BigDecimal novoValorUnitario) {
        if (novoValorUnitario != null && novoValorUnitario.compareTo(BigDecimal.ZERO) > 0) {
            this.valorUnitario = novoValorUnitario;
            calcularValorTotal();
        }
    }

    // Getters e Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getProjetoId() {
        return projetoId;
    }

    public void setProjetoId(Integer projetoId) {
        this.projetoId = projetoId;
    }

    public Projeto getProjeto() {
        return projeto;
    }

    public void setProjeto(Projeto projeto) {
        this.projeto = projeto;
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
        calcularValorTotal();
    }

    public BigDecimal getValorUnitario() {
        return valorUnitario;
    }

    public void setValorUnitario(BigDecimal valorUnitario) {
        this.valorUnitario = valorUnitario;
        calcularValorTotal();
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
