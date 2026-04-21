package com.joao.osMarmoraria.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.joao.osMarmoraria.domain.Cliente;
import com.joao.osMarmoraria.domain.Projeto;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class VendaProjetoDTO {

    private Integer id;

    @NotNull(message = "Cliente é obrigatório")
    private Integer clienteId;

    private Cliente cliente;

    @NotNull(message = "Projeto é obrigatório")
    private Integer projetoId;

    private Projeto projeto;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dataVenda;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dataEfetivacao;

    @NotNull(message = "Valor total é obrigatório")
    @Positive(message = "Valor total deve ser positivo")
    private BigDecimal valorTotal;

    private BigDecimal desconto = BigDecimal.ZERO;

    private BigDecimal valorFinal;

    @NotNull(message = "Forma de pagamento é obrigatória")
    private String formaPagamento; // DINHEIRO, CARTAO_CREDITO, CARTAO_DEBITO, PIX, BOLETO

    private String status; // ORCAMENTO, VENDIDO, CANCELADO

    private String observacoes;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataPrevistaConclusao;

    // Campos calculados/informativos
    private String nomeCliente;
    private String documentoCliente;
    private String nomeProjeto;
    private String tipoProjeto;
    private String descricaoProjeto;

    // Flags de controle
    private Boolean ordemServicoGerada = false;
    private Boolean contaReceberGerada = false;
    private Boolean podeGerarOS = false;
    private Boolean podeGerarContaReceber = false;

    // Informações adicionais
    private Integer numeroOrdemServico;
    private String statusOrdemServico;
    private BigDecimal valorPendente;
    private BigDecimal valorRecebido;

    // Construtor padrão
    public VendaProjetoDTO() {
    }

    // Método para calcular valor final
    public BigDecimal getValorFinal() {
        if (valorTotal != null && desconto != null) {
            return valorTotal.subtract(desconto);
        }
        return valorTotal;
    }

    // Método para verificar se pode ser efetivada
    public Boolean podeSerEfetivada() {
        return "ORCAMENTO".equals(status) && dataEfetivacao == null;
    }

    // Método para verificar se está efetivada
    public Boolean isEfetivada() {
        return "VENDIDO".equals(status) && dataEfetivacao != null;
    }
}

