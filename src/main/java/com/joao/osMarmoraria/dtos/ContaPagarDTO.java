package com.joao.osMarmoraria.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.joao.osMarmoraria.domain.Compra;
import com.joao.osMarmoraria.domain.ContaPagar;
import com.joao.osMarmoraria.domain.Fornecedor;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

@Data
public class ContaPagarDTO {

    private Integer id;

    @NotNull(message = "Compra é obrigatória")
    private Integer compraId;

    private Compra compra;

    private Fornecedor fornecedor;

    @NotNull(message = "Valor é obrigatório")
    @Positive(message = "Valor deve ser positivo")
    private BigDecimal valor;

    @NotNull(message = "Data de vencimento é obrigatória")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataVencimento;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataPagamento;

    private String status; // PENDENTE, PAGO, VENCIDO, CANCELADO

    @JsonProperty("descricao")
    private String observacoes;

    private String numeroDocumento;

    private String formaPagamento;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDate dataCriacao;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDate dataAtualizacao;

    private String usuarioCriacao;

    private Integer diasVencimento;
    private Integer diasAtraso;
    private Boolean vencida;
    private BigDecimal valorPago;
    private BigDecimal valorPendente;

    public void setDiasAtraso(int diasAtraso) {
        this.diasAtraso = diasAtraso;
    }

    public ContaPagarDTO() {
    }

    public ContaPagarDTO(ContaPagar obj) {
        this.id = obj.getId();
        this.compraId = obj.getCompra() != null ? obj.getCompra().getComprId() : null;
        this.compra = obj.getCompra();
        this.fornecedor = obj.getCompra() != null ? obj.getCompra().getFornecedor() : null;
        this.valor = obj.getValor();
        this.dataVencimento = obj.getDataVencimento();
        this.dataPagamento = obj.getDataPagamento();
        this.status = obj.getStatus();
        this.observacoes = obj.getObservacoes();
        this.numeroDocumento = obj.getNumeroDocumento();
        this.formaPagamento = obj.getFormaPagamento();
        this.dataCriacao = obj.getDataCriacao();
        this.dataAtualizacao = obj.getDataAtualizacao();
        this.usuarioCriacao = obj.getUsuarioCriacao();
        this.diasVencimento = obj.getDiasVencimento();
        this.diasAtraso = obj.getDiasAtraso();
        this.vencida = obj.getVencida();
        this.valorPago = obj.getValorPago();
        this.valorPendente = obj.getValorPendente();
    }
}

