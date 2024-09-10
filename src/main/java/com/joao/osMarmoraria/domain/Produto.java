package com.joao.osMarmoraria.domain;
import com.joao.osMarmoraria.domain.Fornecedor;
import com.joao.osMarmoraria.domain.Grupo;
import com.joao.osMarmoraria.domain.OrdemDeServico;
import com.joao.osMarmoraria.domain.enums.UnidadeDeMedida;
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
    private BigDecimal precoCusto;
    private BigDecimal precoVenda;
    private BigDecimal lucro;
    private BigDecimal margemLucro;
    private Boolean ativo = true;
    private BigDecimal estoque;
    private BigDecimal quantidade;

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
