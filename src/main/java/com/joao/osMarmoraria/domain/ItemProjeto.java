package com.joao.osMarmoraria.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Data
@AllArgsConstructor
public class ItemProjeto {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private Integer quantidade;
    private BigDecimal largura;
    private BigDecimal altura;
    private BigDecimal profundidade;
    private String acabamento;

    private BigDecimal subTotal;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "prod_id", nullable = false)
    private Produto produto;

    @ManyToOne
    private Projeto projeto;


    public ItemProjeto() {
    }

    public ItemProjeto(Integer itemId) {
    }
}
