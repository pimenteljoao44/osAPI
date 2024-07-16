package com.joao.osMarmoraria.dtos;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.joao.osMarmoraria.domain.OrdemDeServico;
import com.joao.osMarmoraria.domain.Produto;
import com.joao.osMarmoraria.domain.Servico;
import com.joao.osMarmoraria.domain.enums.Prioridade;
import com.joao.osMarmoraria.domain.enums.Status;

import javax.validation.constraints.NotEmpty;

public class OsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime dataAbertura;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime dataFechamento;

    private Integer prioridade;
    private Integer status;
    private Integer funcionario;
    private Integer cliente;

    @NotEmpty(message = "O campo observações é requerido!")
    private String observacoes;

    private String descricao;

    private BigDecimal valorTotal = BigDecimal.ZERO;

    private BigDecimal desconto = BigDecimal.ZERO;

    private List<Integer> produtos;

    private List<Integer> servicos;


    public OsDTO() {
        super();
    }


    public OsDTO(OrdemDeServico obj) {
        super();
        this.id = obj.getId();
        this.dataAbertura = obj.getDataAbertura();
        this.dataFechamento = obj.getDataFechamento();
        this.prioridade = obj.getPrioridade().getCod();
        this.status = obj.getStatus().getCod();
        this.funcionario = obj.getFuncionario().getId();
        this.cliente = obj.getCliente().getCliId();
        this.observacoes = obj.getObservacoes();
        this.descricao = obj.getDescricao();
        this.valorTotal = obj.calculaTotal();
        this.desconto = obj.getDesconto();
        this.produtos = obj.getProdutos().stream()
                .map(Produto::getProdId)
                .collect(Collectors.toList());
        this.servicos = obj.getServicos().stream()
                .map(Servico::getId)
                .collect(Collectors.toList());
    }


    public Integer getId() {
        return id;
    }


    public void setId(Integer id) {
        this.id = id;
    }


    public LocalDateTime getDataAbertura() {
        return dataAbertura;
    }


    public void setDataAbertura(LocalDateTime dataAbertura) {
        this.dataAbertura = dataAbertura;
    }


    public LocalDateTime getDataFechamento() {
        return dataFechamento;
    }


    public void setDataFechamento(LocalDateTime dataFechamento) {
        this.dataFechamento = dataFechamento;
    }


    public Prioridade getPrioridade() {
        return Prioridade.toEnum(this.prioridade);
    }


    public void setPrioridade(Integer prioridade) {
        this.prioridade = prioridade;
    }


    public Status getStatus() {
        return Status.toEnum(this.status);
    }


    public void setStatus(Integer status) {
        this.status = status;
    }


    public Integer getFuncionario() {
        return funcionario;
    }


    public void setFuncionario(Integer funcionario) {
        this.funcionario = funcionario;
    }


    public Integer getCliente() {
        return cliente;
    }


    public void setCliente(Integer cliente) {
        this.cliente = cliente;
    }


    public String getObservacoes() {
        return observacoes;
    }


    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }


    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
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

    public List<Integer> getProdutos() {
        return produtos;
    }

    public void setProdutos(List<Integer> produtos) {
        this.produtos = produtos;
    }

    public List<Integer> getServicos() {
        return servicos;
    }

    public void setServicos(List<Integer> servicos) {
        this.servicos = servicos;
    }
}
