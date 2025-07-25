package com.joao.osMarmoraria.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RelatorioContasReceberDTO {

    private Long clienteId;

    private LocalDate dataVencimentoInicio;

    private LocalDate dataVencimentoFim;

    private Boolean apenasVencidas;

    private Boolean apenasRecebidas;
}

