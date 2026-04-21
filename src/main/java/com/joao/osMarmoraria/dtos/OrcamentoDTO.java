package com.joao.osMarmoraria.dtos;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrcamentoDTO {

    private BigDecimal area;
    private BigDecimal valorMaterial;
    private BigDecimal valorMaoObra;
    private BigDecimal valorTotal;
    private BigDecimal margemLucro;
}
