package com.joao.osMarmoraria.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@AllArgsConstructor
@JsonIgnoreProperties({"grupoPai"})
public class Grupo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private String nome;
    private Boolean ativo = true;
    @ManyToOne
    @JoinColumn(name = "grupo_pai")
    @JsonBackReference
    private Grupo grupoPai;

    public Grupo() {
    }

    @Override
    public String toString() {
        return "Grupo{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", ativo=" + ativo +
                ", grupoPai=" + grupoPai +
                '}';
    }
}
