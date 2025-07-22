package com.joao.osMarmoraria.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.joao.osMarmoraria.domain.Cliente;

import com.joao.osMarmoraria.domain.Projeto;
import com.joao.osMarmoraria.domain.enums.StatusOrdemServico;

import javax.validation.constraints.*;

public class OrdemServicoDTO {

    private Integer id;

    @NotBlank(message = "Número da O.S. é obrigatório")
    @Size(max = 20, message = "Número deve ter no máximo 20 caracteres")
    private String numero;

    @NotNull(message = "Projeto é obrigatório")
    private Integer projetoId;

    private Projeto projeto;

    @NotNull(message = "Cliente é obrigatório")
    private Integer clienteId;

    private Cliente cliente;

    @NotNull(message = "Data de emissão é obrigatória")
    private LocalDate dataEmissao;

    private LocalDate dataPrevistaInicio;

    private LocalDate dataPrevistaConclusao;

    private LocalDate dataInicio;

    private LocalDate dataConclusao;

    private StatusOrdemServico status = StatusOrdemServico.PENDENTE;

    @Size(max = 100, message = "Responsável deve ter no máximo 100 caracteres")
    private String responsavel;

    private String observacoes;

    private String instrucoesTecnicas;

    @NotNull(message = "Valor total é obrigatório")
    @DecimalMin(value = "0.0", inclusive = false, message = "Valor total deve ser maior que zero")
    private BigDecimal valorTotal;

    private List<ItemOrdemServicoDTO> itens;

    private LocalDateTime dataCriacao;

    private LocalDateTime dataAtualizacao;

    @NotNull(message = "Usuário de criação é obrigatório")
    private Integer usuarioCriacao;

    // Construtores
    public OrdemServicoDTO() {
    }

    // Getters e Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
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

    public LocalDate getDataEmissao() {
        return dataEmissao;
    }

    public void setDataEmissao(LocalDate dataEmissao) {
        this.dataEmissao = dataEmissao;
    }

    public LocalDate getDataPrevistaInicio() {
        return dataPrevistaInicio;
    }

    public void setDataPrevistaInicio(LocalDate dataPrevistaInicio) {
        this.dataPrevistaInicio = dataPrevistaInicio;
    }

    public LocalDate getDataPrevistaConclusao() {
        return dataPrevistaConclusao;
    }

    public void setDataPrevistaConclusao(LocalDate dataPrevistaConclusao) {
        this.dataPrevistaConclusao = dataPrevistaConclusao;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDate getDataConclusao() {
        return dataConclusao;
    }

    public void setDataConclusao(LocalDate dataConclusao) {
        this.dataConclusao = dataConclusao;
    }

    public StatusOrdemServico getStatus() {
        return status;
    }

    public void setStatus(StatusOrdemServico status) {
        this.status = status;
    }

    public String getResponsavel() {
        return responsavel;
    }

    public void setResponsavel(String responsavel) {
        this.responsavel = responsavel;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public String getInstrucoesTecnicas() {
        return instrucoesTecnicas;
    }

    public void setInstrucoesTecnicas(String instrucoesTecnicas) {
        this.instrucoesTecnicas = instrucoesTecnicas;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public List<ItemOrdemServicoDTO> getItens() {
        return itens;
    }

    public void setItens(List<ItemOrdemServicoDTO> itens) {
        this.itens = itens;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }

    public Integer getUsuarioCriacao() {
        return usuarioCriacao;
    }

    public void setUsuarioCriacao(Integer usuarioCriacao) {
        this.usuarioCriacao = usuarioCriacao;
    }
}
