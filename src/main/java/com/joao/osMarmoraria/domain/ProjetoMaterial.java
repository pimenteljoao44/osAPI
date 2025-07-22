package com.joao.osMarmoraria.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "projeto_material")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjetoMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "projeto_id", nullable = false)
    private Projeto projeto;

    @ManyToOne
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Column(precision = 10, scale = 3, nullable = false)
    private BigDecimal quantidade;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal valorUnitario;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal valorTotal;

    @PrePersist
    @PreUpdate
    public void calcularValorTotal() {
        if (quantidade != null && valorUnitario != null) {
            this.valorTotal = quantidade.multiply(valorUnitario);
        }
    }
}

