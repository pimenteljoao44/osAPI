package com.joao.osMarmoraria.dtos;

import com.joao.osMarmoraria.domain.Cliente;
import com.joao.osMarmoraria.domain.Projeto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class VendaProjetoDTO {

    private Integer id;
    private Integer clienteId;
    private Cliente cliente;
    private String nomeCliente;
    private Integer projetoId;
    private Projeto projeto;
    private String nomeProjeto;
    private String tipoProjeto;
    private LocalDateTime dataVenda;
    private LocalDateTime dataEfetivacao;
    private LocalDate dataPrevistaConclusao;
    private BigDecimal valorTotal;
    private BigDecimal desconto;
    private BigDecimal valorFinal;
    private String formaPagamento;
    private Integer numeroParcelas;
    private String observacoes;
    private String status;

    // Flags de controle
    private Boolean ordemServicoGerada = false;
    private Boolean contaReceberGerada = false;
    private Boolean podeGerarOS = false;
    private Boolean podeGerarContaReceber = false;

    // Construtores
    public VendaProjetoDTO() {
    }

    // Getters e Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getClienteId() {
        return clienteId;
    }

    public void setClienteId(Integer clienteId) {
        this.clienteId = clienteId;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public String getNomeCliente() {
        return nomeCliente;
    }

    public void setNomeCliente(String nomeCliente) {
        this.nomeCliente = nomeCliente;
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

    public String getNomeProjeto() {
        return nomeProjeto;
    }

    public void setNomeProjeto(String nomeProjeto) {
        this.nomeProjeto = nomeProjeto;
    }

    public String getTipoProjeto() {
        return tipoProjeto;
    }

    public void setTipoProjeto(String tipoProjeto) {
        this.tipoProjeto = tipoProjeto;
    }

    public LocalDateTime getDataVenda() {
        return dataVenda;
    }

    public void setDataVenda(LocalDateTime dataVenda) {
        this.dataVenda = dataVenda;
    }

    public LocalDateTime getDataEfetivacao() {
        return dataEfetivacao;
    }

    public void setDataEfetivacao(LocalDateTime dataEfetivacao) {
        this.dataEfetivacao = dataEfetivacao;
    }

    public LocalDate getDataPrevistaConclusao() {
        return dataPrevistaConclusao;
    }

    public void setDataPrevistaConclusao(LocalDate dataPrevistaConclusao) {
        this.dataPrevistaConclusao = dataPrevistaConclusao;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public BigDecimal getDesconto() {
        return desconto;
    }

    public void setDesconto(BigDecimal desconto) {
        this.desconto = desconto;
    }

    public BigDecimal getValorFinal() {
        return valorFinal;
    }

    public void setValorFinal(BigDecimal valorFinal) {
        this.valorFinal = valorFinal;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getOrdemServicoGerada() {
        return ordemServicoGerada;
    }

    public void setOrdemServicoGerada(Boolean ordemServicoGerada) {
        this.ordemServicoGerada = ordemServicoGerada;
    }

    public Boolean getContaReceberGerada() {
        return contaReceberGerada;
    }

    public void setContaReceberGerada(Boolean contaReceberGerada) {
        this.contaReceberGerada = contaReceberGerada;
    }

    public Boolean getPodeGerarOS() {
        return podeGerarOS;
    }

    public void setPodeGerarOS(Boolean podeGerarOS) {
        this.podeGerarOS = podeGerarOS;
    }

    public Boolean getPodeGerarContaReceber() {
        return podeGerarContaReceber;
    }

    public void setPodeGerarContaReceber(Boolean podeGerarContaReceber) {
        this.podeGerarContaReceber = podeGerarContaReceber;
    }

    // Métodos auxiliares
    public BigDecimal getValorParcela() {
        if (numeroParcelas != null && numeroParcelas > 0 && valorFinal != null) {
            return valorFinal.divide(BigDecimal.valueOf(numeroParcelas), 2, BigDecimal.ROUND_HALF_UP);
        }
        return valorFinal;
    }

    public boolean isEfetivada() {
        return dataEfetivacao != null;
    }

    public boolean isOrcamento() {
        return "ORCAMENTO".equals(status);
    }

    public boolean isVendida() {
        return "VENDIDO".equals(status);
    }
}