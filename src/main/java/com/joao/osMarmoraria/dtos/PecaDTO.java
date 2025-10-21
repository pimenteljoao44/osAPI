package com.joao.osMarmoraria.dtos;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class PecaDTO implements Serializable {
    private Long id;
    private String nome;
    private String tipo;
    private Double largura;
    private Double altura;
    private Double espessura;
    private String unidade;
    private Double x;
    private Double y;
    private List<RecorteDTO> recortes;
}
