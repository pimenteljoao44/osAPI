package com.joao.osMarmoraria.dtos;

import lombok.Data;

import javax.validation.constraints.*;
import java.math.BigDecimal;

@Data
public class ItemVendaUpdateDTO {

    @DecimalMin(value = "0.01", message = "Quantidade deve ser maior que zero")
    private BigDecimal quantidade;

    @DecimalMin(value = "0.0", inclusive = true, message = "Preço não pode ser negativo")
    private BigDecimal preco;

    @Size(max = 500, message = "Observações não podem exceder 500 caracteres")
    private String observacoes;
}