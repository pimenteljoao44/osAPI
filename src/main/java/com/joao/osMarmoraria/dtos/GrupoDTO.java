package com.joao.osMarmoraria.dtos;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.joao.osMarmoraria.domain.Grupo;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class GrupoDTO implements Serializable {
    private Integer id;

    @NotEmpty(message = "O campo nome é requerido!")
    private String nome;
    private Boolean ativo;

    private Integer grupoPaiId;

    public GrupoDTO(Grupo obj) {
        this.id = obj.getId();
        this.nome = obj.getNome();
        this.ativo = obj.getAtivo();
        this.grupoPaiId = (obj.getGrupoPai() != null) ? obj.getGrupoPai().getId() : null;
    }

    public GrupoDTO() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public @NotEmpty(message = "O campo nome é requerido!") String getNome() {
        return nome;
    }

    public void setNome(@NotEmpty(message = "O campo nome é requerido!") String nome) {
        this.nome = nome;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    // Getter e setter para o ID do grupoPai
    public Integer getGrupoPaiId() {
        return grupoPaiId;
    }

    public void setGrupoPaiId(Integer grupoPaiId) {
        this.grupoPaiId = grupoPaiId;
    }
}
