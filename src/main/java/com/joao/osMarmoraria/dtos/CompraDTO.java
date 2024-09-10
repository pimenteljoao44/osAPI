package com.joao.osMarmoraria.dtos;

import com.joao.osMarmoraria.domain.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class CompraDTO implements Serializable {

    private Integer comprId;
    private String observacoes;
    private BigDecimal valorTotal;
    private BigDecimal quantidadeTotal;
    private Date dataCompra = new Date();
    private Integer fornecedor;
    private Integer funcionario;
    private Integer formaPagamento;
    private List<ItemCompraDTO> itensCompra = new ArrayList<>();

    public CompraDTO(Compra obj) {
        this.comprId = obj.getComprId();
        this.observacoes = obj.getObservacoes();
        this.valorTotal = obj.getValorTotal();
        this.quantidadeTotal = obj.getQuantidadeTotal();
        this.dataCompra = obj.getDataCompra();
        this.fornecedor = obj.getFornecedor().getId();
        this.funcionario = obj.getFuncionario().getId();
        this.formaPagamento = obj.getFormaPagamento().getCod();
        this.itensCompra = obj.getItensCompra().stream()
                .map(ItemCompraDTO::new)
                .collect(Collectors.toList());
    }

    public CompraDTO() {
    }

    public Integer getComprId() {
        return comprId;
    }

    public void setComprId(Integer comprId) {
        this.comprId = comprId;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public BigDecimal getQuantidadeTotal() {
        return quantidadeTotal;
    }

    public void setQuantidadeTotal(BigDecimal quantidadeTotal) {
        this.quantidadeTotal = quantidadeTotal;
    }

    public Date getDataCompra() {
        return dataCompra;
    }

    public void setDataCompra(Date dataCompra) {
        this.dataCompra = dataCompra;
    }

    public Integer getFornecedor() {
        return fornecedor;
    }

    public void setFornecedor(Integer fornecedor) {
        this.fornecedor = fornecedor;
    }

    public Integer getFuncionario() {
        return funcionario;
    }

    public void setFuncionario(Integer funcionario) {
        this.funcionario = funcionario;
    }

    public Integer getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(Integer formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public List<ItemCompraDTO> getItensCompra() {
        return itensCompra;
    }

    public void setItensCompra(List<ItemCompraDTO> itensCompra) {
        this.itensCompra = itensCompra;
    }
}
