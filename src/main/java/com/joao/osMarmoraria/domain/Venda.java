package com.joao.osMarmoraria.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.joao.osMarmoraria.domain.enums.FormaPagamento;
import com.joao.osMarmoraria.domain.enums.VendaTipo;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.dao.DataIntegrityViolationException;

import javax.persistence.*;
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm", timezone = "America/Sao_Paulo")
    private Date dataAbertura;

    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm", timezone = "America/Sao_Paulo")
    private Date dataFechamento;

    private BigDecimal total = BigDecimal.ZERO;
    private BigDecimal desconto = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    private VendaTipo vendaTipo = VendaTipo.VENDA;

    @Enumerated(EnumType.STRING)
    private FormaPagamento formaPagamento = FormaPagamento.DINHEIRO;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "venda", orphanRemoval = true)
    private List<ItemVenda> itensVenda = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name ="cliente_id")
    private Cliente cliente;

    public Venda() {
    }

    public void addItem(ItemVenda item) {
        item.setVenda(this);
        if (!itensVenda.contains(item)) {
            item.setPreco(item.getProduto().getPreco());
            itensVenda.add(item);
            item.getProduto().baixarEstoque(item.getQuantidade());
            calculaTotal();
        } else {
            throw new DataIntegrityViolationException("O produto " + item.getProduto().getNome() + " já está adicionado na venda");
        }
    }

    public void removeItem(ItemVenda item) {
        if (itensVenda.remove(item)) {
            item.getProduto().estornarEstoque(item.getQuantidade());
            calculaTotal();
        }
    }

    public BigDecimal calculaTotal() {
        total = itensVenda.stream()
                .map(item -> item.getPreco().multiply(item.getQuantidade()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
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
}
