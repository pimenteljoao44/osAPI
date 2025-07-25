package com.joao.osMarmoraria.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RelatorioContasPagarDTO {

    private Long fornecedorId;

    private LocalDate dataVencimentoInicio;

    private LocalDate dataVencimentoFim;

    private Boolean apenasVencidas;

    private Boolean apenasQuitadas;
}

