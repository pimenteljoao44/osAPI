package com.joao.osMarmoraria.dtos;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
public class ItemOrcamentoDTO {
    @NotBlank(message = "Nome do item é obrigatório")
    private String nome;

    private String descricao;

    @NotNull(message = "Quantidade é obrigatória")
    @Positive(message = "Quantidade deve ser positiva")
    private BigDecimal quantidade;

    @NotBlank(message = "Unidade de medida é obrigatória")
    private String unidade;

    @NotNull(message = "Valor unitário é obrigatório")
    @Positive(message = "Valor unitário deve ser positivo")
    private BigDecimal valorUnitario;

    @NotNull(message = "Valor total é obrigatório")
    @Positive(message = "Valor total deve ser positivo")
    private BigDecimal valorTotal;
}