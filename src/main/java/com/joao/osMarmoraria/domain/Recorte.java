package com.joao.osMarmoraria.domain;

import javax.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Recorte implements Serializable {

    private String tipo;
    private Double largura;
    private Double altura;
    private Double posicaoX;
    private Double posicaoY;
}
