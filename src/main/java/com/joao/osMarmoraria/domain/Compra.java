package com.joao.osMarmoraria.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.joao.osMarmoraria.domain.enums.FormaPagamento;
import lombok.AllArgsConstructor;
import lombok.Data;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
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
    
    // Novos campos para suporte a parcelamento
    @Column(name = "numero_parcelas")
    private Integer numeroParcelas = 1;
    
    @Column(name = "intervalo_parcelas")
    private Integer intervaloParcelas = 30; // dias entre parcelas

    @JsonManagedReference("compra-itens")
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "compra", orphanRemoval = true)
    private List<ItemCompra> itensCompra = new ArrayList<>();

    @JsonBackReference("compra-contapagar")
    @OneToMany(mappedBy = "compra")
    private List<ContaPagar> contasPagar = new ArrayList<>();

    public Compra() {
    }

    public Compra(Integer comprId, String observacoes, Date dataCompra, Fornecedor fornecedor, Funcionario funcionario, FormaPagamento formaPagamento, List<ItemCompra> itensCompra) {
        this.comprId = comprId;
        this.observacoes = observacoes;
        this.dataCompra = dataCompra;
        this.fornecedor = fornecedor;
        this.funcionario = funcionario;
        this.formaPagamento = formaPagamento;
        this.itensCompra = itensCompra;
    }
    
    // Métodos de conveniência para parcelamento
    public boolean isParcelado() {
        return numeroParcelas != null && numeroParcelas > 1;
    }
    
    public BigDecimal getValorParcela() {
        if (numeroParcelas != null && numeroParcelas > 0 && valorTotal != null) {
            return valorTotal.divide(BigDecimal.valueOf(numeroParcelas), 2, BigDecimal.ROUND_HALF_UP);
        }
        return valorTotal;
    }
}
