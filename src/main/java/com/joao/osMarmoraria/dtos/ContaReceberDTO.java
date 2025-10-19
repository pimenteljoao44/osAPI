package com.joao.osMarmoraria.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.joao.osMarmoraria.domain.Cliente;
import com.joao.osMarmoraria.domain.Projeto;
import com.joao.osMarmoraria.domain.Venda;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ContaReceberDTO {

    private Integer id;

    private Integer vendaId;

    private Venda venda;

    private Integer projetoId;

    private Projeto projeto;

    private Cliente cliente;

    @NotNull(message = "Valor é obrigatório")
    @Positive(message = "Valor deve ser positivo")
    private BigDecimal valor;

    @NotNull(message = "Data de vencimento é obrigatória")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataVencimento;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataPagamento;

    private String status; // PENDENTE, RECEBIDO, VENCIDO, CANCELADO

    @JsonProperty("descricao")
    private String observacoes;

    private String numeroDocumento;

    private String formaPagamento;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dataCriacao;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dataAtualizacao;

    private String usuarioCriacao;

    // Campos calculados
    private Integer diasVencimento;
    private Integer diasAtraso;
    private Boolean vencida;
    private BigDecimal valorRecebido;
    private BigDecimal valorPendente;

    // Informações do cliente
    private String nomeCliente;
    private String documentoCliente;

    private Boolean parcelado;

    private Integer numeroParcelas;


    public void setDiasAtraso(int diasAtraso) {
        this.diasAtraso = diasAtraso;
    }
}
