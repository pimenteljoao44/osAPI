package com.joao.osMarmoraria.dtos;

import com.joao.osMarmoraria.domain.enums.FormaPagamento;
import lombok.Data;

import javax.validation.constraints.*;
import java.math.BigDecimal;

@Data
public class VendaUnificadaUpdateDTO {

    private FormaPagamento formaPagamento;

    @DecimalMin(value = "0.0", inclusive = true, message = "Desconto não pode ser negativo")
    private BigDecimal desconto;

    @Min(value = 1, message = "Número de parcelas deve ser maior que zero")
    @Max(value = 60, message = "Número de parcelas não pode exceder 60")
    private Integer numeroParcelas;

    @Size(max = 1000, message = "Observações não podem exceder 1000 caracteres")
    private String observacoes;
}

