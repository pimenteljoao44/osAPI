package com.joao.osMarmoraria.dtos;

import com.joao.osMarmoraria.domain.ItemVenda;
import com.joao.osMarmoraria.domain.Produto;
import com.joao.osMarmoraria.domain.Venda;

import javax.persistence.CascadeType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.io.Serializable;
import java.math.BigDecimal;

public class ItemVendaDTO implements Serializable {
    private Integer id;

    private BigDecimal quantidade;

    private BigDecimal preco;

    private Produto produto;

    private Integer venda;

    public ItemVendaDTO(ItemVenda obj) {
        this.id = obj.getId();
        this.quantidade = obj.getQuantidade();
        this.preco = obj.getPreco();
        this.produto = obj.getProduto();
        this.venda = obj.getVenda().getVenId();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(BigDecimal quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public Integer getVenda() {
        return venda;
    }

    public void setVenda(Integer venda) {
        this.venda = venda;
    }
}
