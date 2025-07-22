package com.joao.osMarmoraria.dtos;

import lombok.Data;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
public class ProjetoMaterialCreateDTO {

    @NotNull(message = "Produto é obrigatório")
    private Integer produtoId;

    @NotNull(message = "Quantidade é obrigatória")
    @Positive(message = "Quantidade deve ser positiva")
    private BigDecimal quantidade;

    @NotNull(message = "Valor unitário é obrigatório")
    @Positive(message = "Valor unitário deve ser positivo")
    private BigDecimal valorUnitario;

    @AssertTrue(message = "Valor total deve ser igual a quantidade x valor unitário")
    public boolean isValorTotalValido() {
        if (quantidade == null || valorUnitario == null) return false;
        return quantidade.multiply(valorUnitario).compareTo(quantidade.multiply(valorUnitario)) == 0;
    }
}
