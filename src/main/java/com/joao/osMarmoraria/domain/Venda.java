package com.joao.osMarmoraria.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.joao.osMarmoraria.domain.enums.FormaPagamento;
import com.joao.osMarmoraria.domain.enums.VendaTipo;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.dao.DataIntegrityViolationException;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
public class Venda {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer venId;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dataAbertura;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dataFechamento;

    private BigDecimal total = BigDecimal.ZERO;
    private BigDecimal desconto = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    private VendaTipo vendaTipo = VendaTipo.VENDA;

    @Enumerated(EnumType.STRING)
    private FormaPagamento formaPagamento = FormaPagamento.DINHEIRO;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "venda", orphanRemoval = true)
    private List<ItemVenda> itensVenda = new ArrayList<>();

    @JsonBackReference
    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL)
    private List<ContaReceber> contasReceber;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name ="cliente_id")
    private Cliente cliente;

    // Novo campo para vendas de projeto
    @Column(name = "projeto_id")
    private Integer projetoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projeto_id", insertable = false, updatable = false)
    private Projeto projeto;

    // Campos adicionais para vendas de projeto
    private Integer numeroParcelas = 1;
    
    @Column(name = "intervalo_parcelas")
    private Integer intervaloParcelas = 30; // dias entre parcelas
    
    private String observacoes;

    public Venda() {
    }

    public Venda(Integer id, Date dataAbertura, Date dataFechamento, BigDecimal total, BigDecimal desconto, VendaTipo anEnum, FormaPagamento anEnum1, List<ItemVenda> itensVenda, Cliente cliente) {
        this.venId = id;
        this.dataAbertura = dataAbertura;
        this.dataFechamento = dataFechamento;
        this.total = total;
        this.desconto = desconto;
        this.vendaTipo = anEnum;
        this.formaPagamento = anEnum1;
        this.itensVenda = itensVenda;
        this.cliente = cliente;
    }

    public void addItem(ItemVenda item) {
        item.setVenda(this);
        if (!itensVenda.contains(item)) {
            item.setPreco(item.getProduto().getPrecoVenda());
            itensVenda.add(item);
            item.getProduto().baixarEstoque(item.getQuantidade());
            calculaTotal();
        } else {
            throw new DataIntegrityViolationException("O produto " + item.getProduto().getNome() + " já está adicionado na venda");
        }
    }

    public void removeItem(ItemVenda item) {
        if (itensVenda.remove(item)) {
            item.getProduto().aumentarEstoque(item.getQuantidade());
            calculaTotal();
        }
    }

    public BigDecimal calculaTotal() {
        if (vendaTipo == VendaTipo.ORCAMENTO && projeto != null) {
            // Para vendas de projeto, usar o valor do projeto
            total = projeto.getValorTotal();
        } else {
            // Para vendas de produtos, calcular baseado nos itens
            total = itensVenda.stream()
                    .map(item -> item.getProduto().getPrecoCusto().multiply(item.getQuantidade()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        if (desconto != null && desconto.compareTo(total) < 0) {
            total = total.subtract(desconto);
        } else {
            desconto = BigDecimal.ZERO;
        }
        return total;
    }

    public void efetuarVenda() {
        this.dataFechamento = new Date();
        calculaTotal();
    }

    // Métodos específicos para vendas de projeto
    public boolean isVendaProjeto() {
        return vendaTipo == VendaTipo.ORCAMENTO && projetoId != null;
    }

    public boolean isVendaProduto() {
        return vendaTipo == VendaTipo.VENDA && !itensVenda.isEmpty();
    }

    public BigDecimal getValorParcela() {
        if (numeroParcelas != null && numeroParcelas > 0) {
            return total.divide(BigDecimal.valueOf(numeroParcelas), 2, BigDecimal.ROUND_HALF_UP);
        }
        return total;
    }
}