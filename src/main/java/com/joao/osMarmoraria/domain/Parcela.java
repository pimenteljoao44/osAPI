package com.joao.osMarmoraria.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "parcela")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Parcela {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    
    @Column(name = "numero_parcela", nullable = false)
    private Integer numeroParcela;
    
    @Column(name = "total_parcelas", nullable = false)
    private Integer totalParcelas;
    
    @Column(name = "valor_parcela", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorParcela;
    
    @Column(name = "data_vencimento", nullable = false)
    private LocalDate dataVencimento;
    
    @Column(name = "data_pagamento")
    private LocalDate dataPagamento;
    
    @Column(name = "status", nullable = false)
    private String status = "PENDENTE";
    
    @Column(name = "observacoes", length = 500)
    private String observacoes;
    
    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao = LocalDateTime.now();
    
    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;
    
    // Relacionamento com ContaPagar (opcional)
    @JsonBackReference("contapagar-parcelas")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conta_pagar_id")
    private ContaPagar contaPagar;
    
    // Relacionamento com ContaReceber (opcional)
    @JsonBackReference("contareceber-parcelas")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conta_receber_id")
    private ContaReceber contaReceber;
    
    // Métodos de conveniência
    public boolean isPaga() {
        return "PAGO".equals(status) && dataPagamento != null;
    }
    
    public boolean isPendente() {
        return "PENDENTE".equals(status);
    }
    
    public boolean isVencida() {
        return isPendente() && dataVencimento.isBefore(LocalDate.now());
    }
    
    public void marcarComoPaga(LocalDate dataPagamento) {
        this.dataPagamento = dataPagamento;
        this.status = "PAGO";
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    public void cancelar() {
        this.status = "CANCELADO";
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    @PreUpdate
    public void preUpdate() {
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    // Construtor para criação de parcela
    public Parcela(Integer numeroParcela, Integer totalParcelas, BigDecimal valorParcela, 
                   LocalDate dataVencimento, ContaPagar contaPagar) {
        this.numeroParcela = numeroParcela;
        this.totalParcelas = totalParcelas;
        this.valorParcela = valorParcela;
        this.dataVencimento = dataVencimento;
        this.contaPagar = contaPagar;
        this.status = "PENDENTE";
        this.dataCriacao = LocalDateTime.now();
    }
    
    public Parcela(Integer numeroParcela, Integer totalParcelas, BigDecimal valorParcela, 
                   LocalDate dataVencimento, ContaReceber contaReceber) {
        this.numeroParcela = numeroParcela;
        this.totalParcelas = totalParcelas;
        this.valorParcela = valorParcela;
        this.dataVencimento = dataVencimento;
        this.contaReceber = contaReceber;
        this.status = "PENDENTE";
        this.dataCriacao = LocalDateTime.now();
    }
}

