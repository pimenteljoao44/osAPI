package com.joao.osMarmoraria.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParcelaDTO {
    
    private Integer id;
    
    @NotNull(message = "Número da parcela é obrigatório")
    @Positive(message = "Número da parcela deve ser positivo")
    private Integer numeroParcela;
    
    @NotNull(message = "Total de parcelas é obrigatório")
    @Positive(message = "Total de parcelas deve ser positivo")
    private Integer totalParcelas;
    
    @NotNull(message = "Valor da parcela é obrigatório")
    @Positive(message = "Valor da parcela deve ser positivo")
    private BigDecimal valorParcela;
    
    @NotNull(message = "Data de vencimento é obrigatória")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataVencimento;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataPagamento;
    
    @NotNull(message = "Status é obrigatório")
    private String status;
    
    private String observacoes;
    
    private Integer contaPagarId;
    private Integer contaReceberId;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataCriacao;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataAtualizacao;
    
    // Campos adicionais para exibição
    private String descricaoContaPagar;
    private String descricaoContaReceber;
    private String nomeCliente;
    private String nomeFornecedor;
    private BigDecimal valorTotal;
    
    // Campos calculados
    private Boolean vencida;
    private Boolean paga;
    private Integer diasAteVencimento;
    
    // Construtor para criação básica
    public ParcelaDTO(Integer numeroParcela, Integer totalParcelas, BigDecimal valorParcela, 
                      LocalDate dataVencimento, String status) {
        this.numeroParcela = numeroParcela;
        this.totalParcelas = totalParcelas;
        this.valorParcela = valorParcela;
        this.dataVencimento = dataVencimento;
        this.status = status;
    }
    
    // Métodos de conveniência
    public boolean isPaga() {
        return "PAGO".equals(status) && dataPagamento != null;
    }
    
    public boolean isPendente() {
        return "PENDENTE".equals(status);
    }
    
    public boolean isVencida() {
        return isPendente() && dataVencimento != null && dataVencimento.isBefore(LocalDate.now());
    }
    
    public String getDescricaoStatus() {
        switch (status) {
            case "PAGO":
                return "Pago";
            case "PENDENTE":
                return "Pendente";
            case "CANCELADO":
                return "Cancelado";
            default:
                return status;
        }
    }
    
    public String getDescricaoParcela() {
        return numeroParcela + "/" + totalParcelas;
    }
}

