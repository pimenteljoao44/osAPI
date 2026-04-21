package com.joao.osMarmoraria.dtos;

import com.joao.osMarmoraria.domain.Cliente;
import com.joao.osMarmoraria.domain.enums.StatusProjeto;
import com.joao.osMarmoraria.domain.enums.TipoProjeto;
import lombok.Data;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ProjetoDTO {

    private Integer id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    private String nome;

    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    private String descricao;

    @NotNull(message = "Cliente é obrigatório")
    private Integer clienteId;

    private Cliente cliente;

    @NotNull(message = "Tipo de projeto é obrigatório")
    private TipoProjeto tipoProjeto;

    private StatusProjeto status = StatusProjeto.ORCAMENTO;

    private LocalDate dataInicio;

    private LocalDate dataPrevista;

    private LocalDate dataFinalizacao;

    @NotNull(message = "Valor total é obrigatório")
    @DecimalMin(value = "0.0", inclusive = false, message = "Valor total deve ser maior que zero")
    private BigDecimal valorTotal = BigDecimal.ZERO;

    @NotNull(message = "Valor da mão de obra é obrigatório")
    @DecimalMin(value = "0.0", inclusive = true, message = "Valor da mão de obra não pode ser negativo")
    private BigDecimal valorMaoObra = BigDecimal.ZERO;

    @NotNull(message = "Margem de lucro é obrigatória")
    @DecimalMin(value = "0.0", inclusive = true, message = "Margem de lucro não pode ser negativa")
    @DecimalMax(value = "100.0", inclusive = true, message = "Margem de lucro não pode ser maior que 100%")
    private BigDecimal margemLucro = new BigDecimal("20.00");

    private String observacoes;

    private MedidasProjetoDTO medidas;

    private List<ProjetoItemDTO> itens;

    private LocalDateTime dataCriacao;

    private LocalDateTime dataAtualizacao;

    @NotNull(message = "Usuário de criação é obrigatório")
    private Integer usuarioCriacao;

    // Construtores
    public ProjetoDTO() {
    }

    // Getters e Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
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

    public TipoProjeto getTipoProjeto() {
        return tipoProjeto;
    }

    public void setTipoProjeto(TipoProjeto tipoProjeto) {
        this.tipoProjeto = tipoProjeto;
    }

    public StatusProjeto getStatus() {
        return status;
    }

    public void setStatus(StatusProjeto status) {
        this.status = status;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDate getDataPrevista() {
        return dataPrevista;
    }

    public void setDataPrevista(LocalDate dataPrevista) {
        this.dataPrevista = dataPrevista;
    }

    public LocalDate getDataFinalizacao() {
        return dataFinalizacao;
    }

    public void setDataFinalizacao(LocalDate dataFinalizacao) {
        this.dataFinalizacao = dataFinalizacao;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
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

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public MedidasProjetoDTO getMedidas() {
        return medidas;
    }

    public void setMedidas(MedidasProjetoDTO medidas) {
        this.medidas = medidas;
    }

    public List<ProjetoItemDTO> getItens() {
        return itens;
    }

    public void setItens(List<ProjetoItemDTO> itens) {
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
