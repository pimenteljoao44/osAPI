package com.joao.osMarmoraria.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
public class ContaReceber implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    
    @JsonManagedReference
    @ManyToOne
    @JoinColumn(name = "venda_id")
    private Venda venda;

    @ManyToOne
    private Projeto projeto;

    private BigDecimal valor;
    private LocalDate dataVencimento;
    private LocalDate dataPagamento;
    private String status;
    
    // Novos campos para suporte a parcelamento
    @Column(name = "parcelado")
    private Boolean parcelado = false;
    
    @Column(name = "numero_parcelas")
    private Integer numeroParcelas = 1;
    
    // Relacionamento com parcelas
    @JsonManagedReference("contareceber-parcelas")
    @OneToMany(mappedBy = "contaReceber", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Parcela> parcelas = new ArrayList<>();

    public ContaReceber() {
    }
    
    // Métodos de conveniência para parcelamento
    public boolean isParcelado() {
        return parcelado != null && parcelado && numeroParcelas > 1;
    }
    
    public BigDecimal getValorRecebido() {
        if (isParcelado()) {
            return parcelas.stream()
                    .filter(Parcela::isPaga)
                    .map(Parcela::getValorParcela)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        return "PAGO".equals(status) ? valor : BigDecimal.ZERO;
    }
    
    public BigDecimal getValorPendente() {
        return valor.subtract(getValorRecebido());
    }
    
    public long getParcelasRecebidas() {
        return parcelas.stream().filter(Parcela::isPaga).count();
    }
    
    public long getParcelasPendentes() {
        return parcelas.stream().filter(Parcela::isPendente).count();
    }
}
