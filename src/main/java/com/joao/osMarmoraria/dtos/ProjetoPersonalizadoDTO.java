package com.joao.osMarmoraria.dtos;

import com.joao.osMarmoraria.domain.enums.TipoProjeto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;

public class ProjetoPersonalizadoDTO {

    @NotNull(message = "Nome do projeto é obrigatório")
    private String nome;

    private String descricao;

    @NotNull(message = "Tipo do projeto é obrigatório")
    private TipoProjeto tipoProjeto;

    @NotNull(message = "Cliente é obrigatório")
    private Integer clienteId;

    @NotNull(message = "Profundidade é obrigatória")
    @Positive(message = "Profundidade deve ser positiva")
    private BigDecimal profundidade;

    @NotNull(message = "Largura é obrigatória")
    @Positive(message = "Largura deve ser positiva")
    private BigDecimal largura;

    @NotNull(message = "Altura é obrigatória")
    @Positive(message = "Altura deve ser positiva")
    private BigDecimal altura;

    @NotNull(message = "Margem de lucro é obrigatória")
    @Positive(message = "Margem de lucro deve ser positiva")
    private BigDecimal margemLucro;

    private String observacoes;

    @NotNull(message = "Lista de materiais é obrigatória")
    private List<MaterialProjetoDTO> materiais;

    private List<AcabamentoProjetoDTO> acabamentos;

    // Getters e Setters
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

    public TipoProjeto getTipoProjeto() {
        return tipoProjeto;
    }

    public void setTipoProjeto(TipoProjeto tipoProjeto) {
        this.tipoProjeto = tipoProjeto;
    }

    public Integer getClienteId() {
        return clienteId;
    }

    public void setClienteId(Integer clienteId) {
        this.clienteId = clienteId;
    }

    public BigDecimal getProfundidade() {
        return profundidade;
    }

    public void setProfundidade(BigDecimal profundidade) {
        this.profundidade = profundidade;
    }

    public BigDecimal getLargura() {
        return largura;
    }

    public void setLargura(BigDecimal largura) {
        this.largura = largura;
    }

    public BigDecimal getAltura() {
        return altura;
    }

    public void setAltura(BigDecimal altura) {
        this.altura = altura;
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

    public List<MaterialProjetoDTO> getMateriais() {
        return materiais;
    }

    public void setMateriais(List<MaterialProjetoDTO> materiais) {
        this.materiais = materiais;
    }

    public List<AcabamentoProjetoDTO> getAcabamentos() {
        return acabamentos;
    }

    public void setAcabamentos(List<AcabamentoProjetoDTO> acabamentos) {
        this.acabamentos = acabamentos;
    }
}

