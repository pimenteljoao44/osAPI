package com.joao.osMarmoraria.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
public class Fornecedor {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @OneToOne(cascade = {CascadeType.ALL, CascadeType.REMOVE}, orphanRemoval = true)
    @JoinColumn(name = "pessoa_id", nullable = false)
    private Pessoa pessoa;

    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm", timezone = "America/Sao_Paulo")
    private Date dataCriacao;

    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm", timezone = "America/Sao_Paulo")
    private Date dataAtualizacao;

    @OneToMany(mappedBy ="fornecedor" )
    private List<Produto> produtos;

    public Fornecedor() {
    }
}
