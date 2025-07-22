package com.joao.osMarmoraria.dtos;

import com.joao.osMarmoraria.domain.Cidade;
import lombok.Data;

@Data
public class CidadeInputDTO {
    private Integer id;
    private String nome;
    private String uf;

    public CidadeInputDTO() {

    }

    public CidadeInputDTO(Cidade cidade) {
        this.id = cidade.getId();
        this.nome = cidade.getNome();
        this.uf = cidade.getUf();
    }


}
