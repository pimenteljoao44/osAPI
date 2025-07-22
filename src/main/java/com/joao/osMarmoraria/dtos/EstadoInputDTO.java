package com.joao.osMarmoraria.dtos;

import com.joao.osMarmoraria.domain.Estado;
import lombok.Data;

@Data
public class EstadoInputDTO {
    private Integer id;
    private String nome;
    private String sigla;

    public EstadoInputDTO() {
    }

    public EstadoInputDTO(Estado estado) {
        this.id = estado.getId();
        this.nome = estado.getNome();
        this.sigla = estado.getSigla();
    }
}
