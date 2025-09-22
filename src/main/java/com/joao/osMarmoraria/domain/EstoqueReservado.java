package com.joao.osMarmoraria.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "estoque_reservado")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstoqueReservado implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantidade;

    @Column(name = "data_reserva", nullable = false)
    private LocalDateTime dataReserva;

    @Column(name = "data_expiracao")
    private LocalDateTime dataExpiracao;

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

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @PrePersist
    protected void onCreate() {
        if (dataReserva == null) {
            dataReserva = LocalDateTime.now();
        }
        if (ativo == null) {
            ativo = true;
        }
    }

    public EstoqueReservado(Produto produto, BigDecimal quantidade, Integer vendaId,
                            Integer projetoId, String observacao) {
        this.produto = produto;
        this.quantidade = quantidade;
        this.vendaId = vendaId;
        this.projetoId = projetoId;
        this.observacao = observacao;
        this.dataReserva = LocalDateTime.now();
        this.ativo = true;
    }

    public void liberar() {
        this.ativo = false;
    }

    public boolean isExpirada() {
        return dataExpiracao != null && LocalDateTime.now().isAfter(dataExpiracao);
    }
}
