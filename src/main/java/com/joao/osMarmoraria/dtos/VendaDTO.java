package com.joao.osMarmoraria.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.joao.osMarmoraria.domain.ItemVenda;
import com.joao.osMarmoraria.domain.Venda;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class VendaDTO implements Serializable {

    private Integer id;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    private Date dataAbertura;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    private Date dataFechamento;

    private BigDecimal total = BigDecimal.ZERO;

    private BigDecimal desconto = BigDecimal.ZERO;

    private Integer vendaTipo;

    private Integer formaPagamento;

    private List<Integer> itensVenda = new ArrayList<>();

    private Integer cliente;

    public VendaDTO(Venda obj) {
        this.id = obj.getVenId();
        this.cliente = obj.getCliente().getCliId();
        this.dataAbertura = obj.getDataAbertura();
        this.dataFechamento = obj.getDataFechamento();
        this.vendaTipo = obj.getVendaTipo().getCod();
        this.formaPagamento = obj.getFormaPagamento().getCod();
        this.desconto = obj.getDesconto();
        this.total = obj.calculaTotal();
        this.itensVenda = obj.getItensVenda().stream()
                .map(ItemVenda::getId)
                .collect(Collectors.toList());
    }

    public VendaDTO() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getDataAbertura() {
        return dataAbertura;
    }

    public void setDataAbertura(Date dataAbertura) {
        this.dataAbertura = dataAbertura;
    }

    public Date getDataFechamento() {
        return dataFechamento;
    }

    public void setDataFechamento(Date dataFechamento) {
        this.dataFechamento = dataFechamento;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getDesconto() {
        return desconto;
    }

    public void setDesconto(BigDecimal desconto) {
        this.desconto = desconto;
    }

    public Integer getVendaTipo() {
        return vendaTipo;
    }

    public void setVendaTipo(Integer vendaTipo) {
        this.vendaTipo = vendaTipo;
    }

    public Integer getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(Integer formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public List<Integer> getItensVenda() {
        return itensVenda;
    }

    public void setItensVenda(List<Integer> itensVenda) {
        this.itensVenda = itensVenda;
    }

    public Integer getCliente() {
        return cliente;
    }

    public void setCliente(Integer cliente) {
        this.cliente = cliente;
    }
}
