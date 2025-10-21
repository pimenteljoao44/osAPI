package com.joao.osMarmoraria.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class RecorteDTO implements Serializable {
    private String tipo;
    private Double largura;
    private Double altura;
    private Double posicaoX;
    private Double posicaoY;
}
