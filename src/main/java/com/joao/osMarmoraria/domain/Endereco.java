package com.joao.osMarmoraria.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "endereco")
@Data
@EqualsAndHashCode(of = "enderecoId")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "enderecoId")
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
public class Endereco implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer enderecoId;

    private String rua;
    private String numero;
    private String complemento;
    private String bairro;

    @ManyToOne()
    @JoinColumn(name = "cidId", nullable = true)
    private Cidade cidade;

    @ManyToOne
    @JoinColumn(name = "estado_id")
    private Estado estado;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE,orphanRemoval = true)
    @JoinColumn(name = "pessoa_id", referencedColumnName = "id")
    private Pessoa pessoa;

    public Endereco() {

    }

    @Override
    public String toString() {
        return "Endereco{" +
                "enderecoId=" + enderecoId +
                ", rua='" + rua + '\'' +
                ", numero='" + numero + '\'' +
                ", complemento='" + complemento + '\'' +
                ", bairro='" + bairro + '\'' +
                ", cidade=" + (cidade != null ? cidade.getId() : null) +
                ", pessoa=" + (pessoa != null ? pessoa.getId() : null) +
                '}';
    }
}
