package com.joao.osMarmoraria.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "pessoa_fisica")
public class PessoaFisica extends Pessoa implements Serializable {

    private static final long serialVersionUID = 1L;
    @Column(name = "pes_cpf", unique = true)
    private String cpf;
    @Column(name = "pes_rg", unique = true)
    private String rg;

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getRg() {
        return rg;
    }

    public void setRg(String rg) {
        this.rg = rg;
    }

    public PessoaFisica() {
    }

    public PessoaFisica(Integer id, String nome,Endereco endereco, String telefone, String cpf, String rg) {
        super(id, nome, endereco, telefone);
        this.cpf = cpf;
        this.rg = rg;
    }

    public PessoaFisica(String cpf, String rg) {
        this.cpf = cpf;
        this.rg = rg;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PessoaFisica that = (PessoaFisica) o;
        return Objects.equals(cpf, that.cpf) && Objects.equals(rg, that.rg);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), cpf, rg);
    }

    @Override
    public String toString() {
        return getId().toString();
    }
}
