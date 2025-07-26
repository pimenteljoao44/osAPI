package com.joao.osMarmoraria.dtos;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

public class VendaProjetoUpdateDTO {

    @DecimalMin(value = "0.0", inclusive = true, message = "Desconto não pode ser negativo")
    private BigDecimal desconto;

    private String formaPagamento;

    @Min(value = 1, message = "Número de parcelas deve ser maior que zero")
    private Integer numeroParcelas;

    @Size(max = 1000, message = "Observações devem ter no máximo 1000 caracteres")
    private String observacoes;

    // Construtores
    public VendaProjetoUpdateDTO() {
    }

    public VendaProjetoUpdateDTO(BigDecimal desconto, String formaPagamento, Integer numeroParcelas, String observacoes) {
        this.desconto = desconto;
        this.formaPagamento = formaPagamento;
        this.numeroParcelas = numeroParcelas;
        this.observacoes = observacoes;
    }

    // Getters e Setters
    public BigDecimal getDesconto() {
        return desconto;
    }

    public void setDesconto(BigDecimal desconto) {
        this.desconto = desconto;
    }

    public String getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(String formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public Integer getNumeroParcelas() {
        return numeroParcelas;
    }

    public void setNumeroParcelas(Integer numeroParcelas) {
        this.numeroParcelas = numeroParcelas;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}

