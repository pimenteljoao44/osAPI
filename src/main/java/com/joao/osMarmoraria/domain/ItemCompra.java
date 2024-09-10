package com.joao.osMarmoraria.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Data
@AllArgsConstructor
public class ItemCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private BigDecimal quantidade;
    private BigDecimal valor;
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "compra_id")
    private Compra compra;
    @ManyToOne
    @JoinColumn(name = "produto_prod_id")
    private Produto produto;

    public ItemCompra() {
    }

    public ItemCompra(Integer id) {
        this.id = id;
    }
}
