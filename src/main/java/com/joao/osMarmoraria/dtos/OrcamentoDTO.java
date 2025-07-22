package com.joao.osMarmoraria.dtos;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
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
