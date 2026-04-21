package com.joao.osMarmoraria.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InstallmentRequestDTO {
    
    @NotNull(message = "Número de parcelas é obrigatório")
    @Min(value = 1, message = "Número de parcelas deve ser no mínimo 1")
    @Max(value = 24, message = "Número de parcelas deve ser no máximo 24")
    private Integer numeroParcelas;
    
    @NotNull(message = "Intervalo entre parcelas é obrigatório")
    @Min(value = 1, message = "Intervalo deve ser no mínimo 1 dia")
    @Max(value = 365, message = "Intervalo deve ser no máximo 365 dias")
    private Integer intervaloDias;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataPrimeiroVencimento;
    
    @NotNull(message = "Valor total é obrigatório")
    @Positive(message = "Valor total deve ser positivo")
    private BigDecimal valorTotal;
    
    private String observacoes;
    
    // Construtor de conveniência
    public InstallmentRequestDTO(Integer numeroParcelas, Integer intervaloDias, BigDecimal valorTotal) {
        this.numeroParcelas = numeroParcelas;
        this.intervaloDias = intervaloDias;
        this.valorTotal = valorTotal;
        this.dataPrimeiroVencimento = LocalDate.now().plusDays(intervaloDias);
    }
    
    // Métodos de conveniência
    public BigDecimal getValorParcela() {
        if (numeroParcelas != null && numeroParcelas > 0 && valorTotal != null) {
            return valorTotal.divide(BigDecimal.valueOf(numeroParcelas), 2, BigDecimal.ROUND_HALF_UP);
        }
        return valorTotal;
    }
    @JsonIgnore
    public LocalDate getDataPrimeiroVencimento() {
        if (dataPrimeiroVencimento == null) {
            return LocalDate.now().plusDays(intervaloDias != null ? intervaloDias : 30);
        }
        return dataPrimeiroVencimento;
    }
    
    public boolean isParcelado() {
        return numeroParcelas != null && numeroParcelas > 1;
    }
    
    // Validação personalizada
    public boolean isValid() {
        return numeroParcelas != null && numeroParcelas > 0 &&
               intervaloDias != null && intervaloDias > 0 &&
               valorTotal != null && valorTotal.compareTo(BigDecimal.ZERO) > 0;
    }
}

