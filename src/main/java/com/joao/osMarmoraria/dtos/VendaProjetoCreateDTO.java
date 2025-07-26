package com.joao.osMarmoraria.dtos;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

public class VendaProjetoCreateDTO {

    @NotNull(message = "Cliente é obrigatório")
    private Integer clienteId;

    @NotNull(message = "Projeto é obrigatório")
    private Integer projetoId;

    @DecimalMin(value = "0.0", inclusive = true, message = "Desconto não pode ser negativo")
    private BigDecimal desconto = BigDecimal.ZERO;

    @NotNull(message = "Forma de pagamento é obrigatória")
    private String formaPagamento;

    @Min(value = 1, message = "Número de parcelas deve ser maior que zero")
    private Integer numeroParcelas = 1;

    @Size(max = 1000, message = "Observações devem ter no máximo 1000 caracteres")
    private String observacoes;

    // Construtores
    public VendaProjetoCreateDTO() {
    }

    public VendaProjetoCreateDTO(Integer clienteId, Integer projetoId, String formaPagamento) {
        this.clienteId = clienteId;
        this.projetoId = projetoId;
        this.formaPagamento = formaPagamento;
    }

    // Getters e Setters
    public Integer getClienteId() {
        return clienteId;
    }

    public void setClienteId(Integer clienteId) {
        this.clienteId = clienteId;
    }

    public Integer getProjetoId() {
        return projetoId;
    }

    public void setProjetoId(Integer projetoId) {
        this.projetoId = projetoId;
    }

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

