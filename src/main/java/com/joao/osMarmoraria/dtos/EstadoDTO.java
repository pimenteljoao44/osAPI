package com.joao.osMarmoraria.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.joao.osMarmoraria.domain.Cidade;
import com.joao.osMarmoraria.domain.Estado;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class EstadoDTO implements Serializable {

    private Integer id;
    @NotEmpty(message = "O Campo Nome é requerido.")
    private String nome;
    @NotEmpty(message = "O campo Sigla é requerido.")
    private String sigla;

    public EstadoDTO() {
    }

    public EstadoDTO(Estado obj) {
        super();
        this.id = obj.getId();
        this.nome = obj.getNome();
        this.sigla = obj.getSigla();
    }
}
