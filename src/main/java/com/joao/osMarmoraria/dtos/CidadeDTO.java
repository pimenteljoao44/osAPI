package com.joao.osMarmoraria.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.joao.osMarmoraria.domain.Cidade;
import com.joao.osMarmoraria.domain.Estado;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.Date;

public class CidadeDTO implements Serializable {

    private Integer cidId;
    @NotEmpty(message = "O Campo Nome é requerido.")
    private String nome;
    private EstadoDTO estado;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm", timezone = "America/Sao_Paulo")
    private Date dataCriacao;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm", timezone = "America/Sao_Paulo")
    private Date dataAtualizacao;

    public CidadeDTO(Cidade obj) {
        super();
        this.cidId = obj.getCidId();
        this.nome = obj.getNome();
        this.dataCriacao = obj.getDataCriacao();
        this.dataAtualizacao = obj.getDataAtualizacao();

        if (obj.getEstado() != null) {
            this.estado = new EstadoDTO(obj.getEstado());
        }
    }

    public CidadeDTO() {
        super();
    }

    public Integer getCidId() {
        return cidId;
    }

    public void setCidId(Integer cidId) {
        this.cidId = cidId;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public EstadoDTO getEstado() {
        return estado;
    }

    public void setEstado(EstadoDTO estado) {
        this.estado = estado;
    }

    public Date getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(Date dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public Date getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(Date dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }
}
