package com.joao.osMarmoraria.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
public class ContaPagar {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    
    @JsonManagedReference
    @ManyToOne
    private Compra compra;

    private BigDecimal valor;
    private LocalDate dataVencimento;
    private String status;
    private LocalDate dataPagamento;
    
    // Novos campos para suporte a parcelamento
    @Column(name = "parcelado")
    private boolean parcelado = false;
    private Integer numeroParcelas;

    // Campos adicionais para controle
    private String observacoes;
    private String numeroDocumento;
    private String formaPagamento;
    private LocalDate dataCriacao;
    private LocalDate dataAtualizacao;
    private String usuarioCriacao;
    private Integer diasVencimento;
    private Integer diasAtraso;
    private Boolean vencida;

    // Relacionamento com parcelas
    @OneToMany(mappedBy = "contaPagar", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Parcela> parcelas = new ArrayList<>();

    public ContaPagar() {
    }
    
    // Métodos de conveniência para parcelamento
    public boolean isParcelado() {
        return false;
    }
    
    public BigDecimal getValorPago() {
        if (isParcelado()) {
            return parcelas.stream()
                    .filter(Parcela::isPaga)
                    .map(Parcela::getValorParcela)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        return "PAGO".equals(status) ? valor : BigDecimal.ZERO;
    }
    
    public BigDecimal getValorPendente() {
        return valor.subtract(getValorPago());
    }
    
    public long getParcelasPagas() {
        return parcelas.stream().filter(Parcela::isPaga).count();
    }
    
    public long getParcelasPendentes() {
        return parcelas.stream().filter(Parcela::isPendente).count();
    }
}
