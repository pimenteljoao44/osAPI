package com.joao.osMarmoraria.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "pessoa_juridica")
public class PessoaJuridica extends Pessoa implements Serializable {
    private static final long serialVersionUID = 1L;
    @Column(name = "pes_cnpj", unique = true)
    private String cnpj;
    @Column(name = "pes_ie")
    private String ie;


    public PessoaJuridica(Integer id, String nome,Endereco endereco, String telefone, String cnpj) {
        super(id, nome, endereco, telefone);
        this.cnpj = cnpj;
    }

    public PessoaJuridica(String cnpj) {
        this.cnpj = cnpj;
    }

    public PessoaJuridica() {

    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getIe() {
        return ie;
    }

    public void setIe(String ie) {
        this.ie = ie;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PessoaJuridica that = (PessoaJuridica) o;
        return Objects.equals(cnpj, that.cnpj) && Objects.equals(ie, that.ie);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), cnpj, ie);
    }

    @Override
    public String toString() {
        return getId().toString();
    }
}
