package com.joao.osMarmoraria.domain;
import com.joao.osMarmoraria.domain.Fornecedor;
import com.joao.osMarmoraria.domain.Grupo;

import com.joao.osMarmoraria.domain.enums.UnidadeDeMedida;
import lombok.AllArgsConstructor;
import lombok.Data;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Data
@AllArgsConstructor
public class Produto {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer prodId;

    private String nome;
    private BigDecimal precoCusto = BigDecimal.ZERO;
    private BigDecimal precoVenda = BigDecimal.ZERO;
    private BigDecimal lucro = BigDecimal.ZERO;
    private BigDecimal margemLucro = BigDecimal.ZERO;
    private Boolean ativo = true;
    private BigDecimal estoque = BigDecimal.ZERO;
    private BigDecimal quantidade = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    private UnidadeDeMedida unidadeDeMedida;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "grupo_id", nullable = true)
    private Grupo grupo;

    @ManyToOne
    @JoinColumn(name = "fornecedor_id")
    private Fornecedor fornecedor;

    @ManyToOne
    @JoinColumn(name = "os_id")
    private OrdemServico ordemDeServico;

    @ManyToOne
    @JoinColumn(name = "ven_id")
    private Venda venda;


    public Produto() {
    }

    public void baixarEstoque(BigDecimal quantidade) {
        if (estoque.compareTo(quantidade) >= 0) {
            estoque = estoque.subtract(quantidade);
        } else {
            throw new IllegalArgumentException("Estoque insuficiente para o produto: " + nome);
        }
    }

    public void aumentarEstoque(BigDecimal quantidade) {
        this.estoque = this.estoque.add(quantidade);
    }


    public void atualizarLucro() {
        if (precoVenda != null && precoCusto != null) {
            lucro = precoVenda.subtract(precoCusto);
            margemLucro = lucro.divide(precoCusto, 2, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100));
        }
    }

    @Override
    public String toString() {
        return "Produto{" +
                "prodId=" + prodId +
                ", nome='" + nome + '\'' +
                ", precoCusto=" + precoCusto +
                ", precoVenda=" + precoVenda +
                ", estoque=" + estoque +
                '}';
    }
}
