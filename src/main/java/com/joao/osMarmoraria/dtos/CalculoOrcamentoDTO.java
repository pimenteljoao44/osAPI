package com.joao.osMarmoraria.dtos;

import java.math.BigDecimal;

public class CalculoOrcamentoDTO {

    private BigDecimal valorMateriais;

    private BigDecimal valorMaoObra;

    private BigDecimal margemLucro;

    private BigDecimal valorTotal;

    private String observacoes;

    // Construtores
    public CalculoOrcamentoDTO() {
    }

    public CalculoOrcamentoDTO(BigDecimal valorMateriais, BigDecimal valorMaoObra, BigDecimal margemLucro, BigDecimal valorTotal) {
        this.valorMateriais = valorMateriais;
        this.valorMaoObra = valorMaoObra;
        this.margemLucro = margemLucro;
        this.valorTotal = valorTotal;
    }

    // Getters e Setters
    public BigDecimal getValorMateriais() {
        return valorMateriais;
    }

    public void setValorMateriais(BigDecimal valorMateriais) {
        this.valorMateriais = valorMateriais;
    }

    public BigDecimal getValorMaoObra() {
        return valorMaoObra;
    }

    public void setValorMaoObra(BigDecimal valorMaoObra) {
        this.valorMaoObra = valorMaoObra;
    }

    public BigDecimal getMargemLucro() {
        return margemLucro;
    }

    public void setMargemLucro(BigDecimal margemLucro) {
        this.margemLucro = margemLucro;
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

