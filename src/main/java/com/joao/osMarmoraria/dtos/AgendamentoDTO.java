package com.joao.osMarmoraria.dtos;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

public class AgendamentoDTO {

    @NotNull(message = "Data prevista de início é obrigatória")
    private LocalDate dataPrevistaInicio;

    private LocalDate dataPrevistaConclusao;

    @Size(max = 100, message = "Responsável deve ter no máximo 100 caracteres")
    private String responsavel;

    @Size(max = 500, message = "Observações devem ter no máximo 500 caracteres")
    private String observacoes;

    public AgendamentoDTO() {
    }

    public AgendamentoDTO(LocalDate dataPrevistaInicio, LocalDate dataPrevistaConclusao, String responsavel, String observacoes) {
        this.dataPrevistaInicio = dataPrevistaInicio;
        this.dataPrevistaConclusao = dataPrevistaConclusao;
        this.responsavel = responsavel;
        this.observacoes = observacoes;
    }

    // Getters e Setters
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
}
