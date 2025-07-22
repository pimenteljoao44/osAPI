package com.joao.osMarmoraria.dtos;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProjetoMaterialDTO {
    private Integer id;
    private Integer produtoId;
    private String produtoNome;
    private BigDecimal quantidade;
    private BigDecimal valorUnitario;
    private BigDecimal valorTotal;
}
