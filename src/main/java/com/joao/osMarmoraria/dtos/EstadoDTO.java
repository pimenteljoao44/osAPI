package com.joao.osMarmoraria.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.joao.osMarmoraria.domain.Cidade;
import com.joao.osMarmoraria.domain.Estado;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class EstadoDTO implements Serializable {

    private Integer id;
    @NotEmpty(message = "O Campo Nome é requerido.")
    private String nome;
    @NotEmpty(message = "O campo Sigla é requerido.")
    private String sigla;

    private List<Cidade> cidades;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm", timezone = "America/Sao_Paulo")
    private Date dataCriacao;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm", timezone = "America/Sao_Paulo")
    private Date dataAtualizacao;

    public EstadoDTO(Estado obj) {
        super();
        this.id = obj.getEstId();
        this.nome = obj.getNome();
        this.sigla = obj.getSigla();
        this.cidades = obj.getCidades();
        this.dataCriacao = obj.getDataCriacao();
        this.dataAtualizacao = obj.getDataAtualizacao();
    }

    public EstadoDTO() {
        super();
    }

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

    public String getSigla() {
        return sigla;
    }

    public void setSigla(String sigla) {
        this.sigla = sigla;
    }

    public List<Cidade> getCidades() {
        return cidades;
    }

    public void setCidades(List<Cidade> cidades) {
        this.cidades = cidades;
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
