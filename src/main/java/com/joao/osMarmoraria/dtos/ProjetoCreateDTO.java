package com.joao.osMarmoraria.dtos;


import com.joao.osMarmoraria.domain.enums.TipoProjeto;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProjetoCreateDTO {

    @NotBlank(message = "Nome do projeto é obrigatório")
    private String nome;

    private String descricao;

    @NotNull(message = "Tipo do projeto é obrigatório")
    private TipoProjeto tipoProjeto;

    @NotNull(message = "Cliente é obrigatório")
    private Integer clienteId;

    @Positive(message = "Profundidade deve ser positiva")
    private BigDecimal profundidade;

    @Positive(message = "Largura deve ser positiva")
    private BigDecimal largura;

    @Positive(message = "Altura deve ser positiva")
    private BigDecimal altura;

    private BigDecimal valorMaoObra;

    @Positive(message = "Margem de lucro deve ser positiva")
    private BigDecimal margemLucro;

    private LocalDateTime dataPrevisaoEntrega;

    private String observacoes;

    private List<ProjetoMaterialCreateDTO> materiais;

    private List<ProjetoAcabamentoCreateDTO> acabamentos;
}
