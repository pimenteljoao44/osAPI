package com.joao.osMarmoraria.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.joao.osMarmoraria.domain.Cidade;
import com.joao.osMarmoraria.domain.Estado;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.Date;
@Data
public class CidadeDTO implements Serializable {
    private String id;
    private String nome;
    private String uf;

    public CidadeDTO() {}

    public CidadeDTO(Cidade obj) {
        this.id = obj.getId().toString();
        this.nome = obj.getNome();
        this.uf = obj.getUf();
    }
}
