package com.joao.osMarmoraria.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Data
@AllArgsConstructor
public class Produto {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer prodId;

    private String nome;
    private BigDecimal preco;
    private Boolean ativo = true;
    private BigDecimal estoque;
    private BigDecimal quantidade;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "grupo_id", nullable = true)
    private Grupo grupo;

    @ManyToOne
    @JoinColumn(name = "fornecedor_id")
    private Fornecedor fornecedor;

    @ManyToOne
    @JoinColumn(name = "os_id")
    private OrdemDeServico ordemDeServico;

    public Produto() {
    }

    public void baixarEstoque(BigDecimal quantidade) {
        if (estoque.compareTo(quantidade) >= 0) {
            estoque = estoque.subtract(quantidade);
        } else {
            throw new IllegalArgumentException("Estoque insuficiente para o produto: " + nome);
        }
    }

    public void estornarEstoque(BigDecimal quantidade) {
        estoque = estoque.add(quantidade);
    }

    @Override
    public String toString() {
        return "Produto{" +
                "prodId=" + prodId +
                ", nome='" + nome + '\'' +
                ", preco=" + preco +
                ", estoque=" + estoque +
                '}';
    }
}
