package com.joao.osMarmoraria.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"venda"})
public class ItemVenda implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private BigDecimal quantidade;
    private BigDecimal preco;
    private BigDecimal subTotal;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "prod_id", nullable = false)
    private Produto produto;

    @JsonBackReference("venda-itens")
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "venda_id")
    private Venda venda;

    public ItemVenda() {
    }

    public ItemVenda(Integer itemId) {
    }
}