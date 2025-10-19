package com.joao.osMarmoraria.domain;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "item_ordem_servico")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ItemOrdemServico implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ordem_servico_id", nullable = false)
    @NotNull(message = "Ordem de serviço é obrigatória")
    private Integer ordemServicoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordem_servico_id", insertable = false, updatable = false)
    @JsonBackReference("ordemservico-itens")
    private OrdemServico ordemServico;

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
    public ItemOrdemServico() {
    }

    public ItemOrdemServico(Integer ordemServicoId, Integer produtoId, BigDecimal quantidade, BigDecimal valorUnitario) {
        this.ordemServicoId = ordemServicoId;
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

    public OrdemServico getOrdemServico() {
        return ordemServico;
    }

    public void setOrdemServico(OrdemServico ordemServico) {
        this.ordemServico = ordemServico;
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
