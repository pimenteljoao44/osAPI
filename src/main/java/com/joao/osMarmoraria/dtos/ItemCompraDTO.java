package com.joao.osMarmoraria.dtos;

import com.joao.osMarmoraria.domain.ItemCompra;

import java.io.Serializable;
import java.math.BigDecimal;

public class ItemCompraDTO implements Serializable {

    private Integer id;
    private Integer compra;
    private BigDecimal quantidade;
    private BigDecimal valor;
    private Integer produto;

    public ItemCompraDTO(ItemCompra obj) {
        this.id = obj.getId();
        this.compra = obj.getCompra().getComprId();
        this.quantidade = obj.getQuantidade();
        this.valor = obj.getValor();
        this.produto = obj.getProduto().getProdId();
    }

    public ItemCompraDTO() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCompra() {
        return compra;
    }

    public void setCompra(Integer compra) {
        this.compra = compra;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(BigDecimal quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public Integer getProduto() {
        return produto;
    }

    public void setProduto(Integer produto) {
        this.produto = produto;
    }
}
