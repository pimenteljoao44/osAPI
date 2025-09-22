package com.joao.osMarmoraria.domain;

import com.joao.osMarmoraria.domain.enums.TipoMovimentacao;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimentacao_estoque")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovimentacaoEstoque implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMovimentacao tipo;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantidade;

    @Column(name = "estoque_anterior", nullable = false, precision = 10, scale = 2)
    private BigDecimal estoqueAnterior;

    @Column(name = "estoque_atual", nullable = false, precision = 10, scale = 2)
    private BigDecimal estoqueAtual;

    @Column(name = "data_movimentacao", nullable = false)
    private LocalDateTime dataMovimentacao;

    @Column(length = 500)
    private String observacao;

    // Referências para rastreabilidade
    @Column(name = "venda_id")
    private Integer vendaId;

    @Column(name = "ordem_servico_id")
    private Integer ordemServicoId;

    @Column(name = "projeto_id")
    private Integer projetoId;

    @Column(name = "usuario_id")
    private Integer usuarioId;

    @PrePersist
    protected void onCreate() {
        if (dataMovimentacao == null) {
            dataMovimentacao = LocalDateTime.now();
        }
    }

    public MovimentacaoEstoque(Produto produto, TipoMovimentacao tipo, BigDecimal quantidade,
                               BigDecimal estoqueAnterior, BigDecimal estoqueAtual, String observacao) {
        this.produto = produto;
        this.tipo = tipo;
        this.quantidade = quantidade;
        this.estoqueAnterior = estoqueAnterior;
        this.estoqueAtual = estoqueAtual;
        this.observacao = observacao;
        this.dataMovimentacao = LocalDateTime.now();
    }
}
