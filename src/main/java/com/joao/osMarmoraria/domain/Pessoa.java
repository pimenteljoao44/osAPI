package com.joao.osMarmoraria.domain;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class Pessoa implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nome;

    @OneToOne(mappedBy = "pessoa", cascade = {CascadeType.REMOVE,CascadeType.ALL}, orphanRemoval = true, fetch = FetchType.LAZY)
    private Endereco endereco;

    private String telefone;

    public Pessoa() {
    }

    public Pessoa(Integer id, String nome,Endereco endereco, String telefone) {
        this.id = id;
        this.nome = nome;
        this.endereco = endereco;
        this.telefone = telefone;
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

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pessoa pessoa = (Pessoa) o;
        return Objects.equals(id, pessoa.id) && Objects.equals(nome, pessoa.nome) && Objects.equals(endereco, pessoa.endereco) && Objects.equals(telefone, pessoa.telefone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nome, endereco, telefone);
    }
}