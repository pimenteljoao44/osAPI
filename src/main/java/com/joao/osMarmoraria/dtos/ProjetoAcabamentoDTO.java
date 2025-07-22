package com.joao.osMarmoraria.dtos;

import com.joao.osMarmoraria.domain.enums.TipoAcabamento;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProjetoAcabamentoDTO {
    private Integer id;
    private String nome;
    private String descricao;
    private BigDecimal valor;
    private TipoAcabamento tipo;
}
