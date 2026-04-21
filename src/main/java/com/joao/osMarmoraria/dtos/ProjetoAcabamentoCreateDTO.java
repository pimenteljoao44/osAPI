package com.joao.osMarmoraria.dtos;

import com.joao.osMarmoraria.domain.enums.TipoAcabamento;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
public class ProjetoAcabamentoCreateDTO {

    @NotBlank(message = "Nome do acabamento é obrigatório")
    private String nome;

    private String descricao;

    @Positive(message = "Valor deve ser positivo")
    private BigDecimal valor;

    private TipoAcabamento tipo;
}

