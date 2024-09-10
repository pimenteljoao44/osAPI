package com.joao.osMarmoraria.domain;

import com.joao.osMarmoraria.domain.enums.FormaPagamento;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer comprId;
    private String observacoes;
    private BigDecimal valorTotal;
    private BigDecimal quantidadeTotal;
    private Date dataCompra = new Date();
    @ManyToOne
    @JoinColumn(name = "fornecedor_id")
    private Fornecedor fornecedor;
    @ManyToOne
    @JoinColumn(name = "funcionario_funcionario_id")
    private Funcionario funcionario;

    @Enumerated(EnumType.STRING)
    private FormaPagamento formaPagamento = FormaPagamento.DINHEIRO;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "compra", orphanRemoval = true)
    private List<ItemCompra> itensCompra = new ArrayList<>();

    public Compra() {
    }
}
