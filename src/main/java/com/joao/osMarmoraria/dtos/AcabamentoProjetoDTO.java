package com.joao.osMarmoraria.dtos;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

public class AcabamentoProjetoDTO {

    @NotNull(message = "ID do serviço é obrigatório")
    private Integer servicoId;

    @NotNull(message = "Quantidade é obrigatória")
    @Positive(message = "Quantidade deve ser positiva")
    private BigDecimal quantidade;

    // Getters e Setters
    public Integer getServicoId() {
        return servicoId;
    }

    public void setServicoId(Integer servicoId) {
        this.servicoId = servicoId;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(BigDecimal quantidade) {
        this.quantidade = quantidade;
    }
}
