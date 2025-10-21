package com.joao.osMarmoraria.dtos;

import com.joao.osMarmoraria.domain.enums.StatusProjeto;
import com.joao.osMarmoraria.domain.enums.TipoProjeto;
import lombok.Data;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class ProjetoDTO {

    private Integer id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    private String nome;

    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    private String descricao;

    @NotNull(message = "Cliente é obrigatório")
    private Integer clienteId;

    private String clienteNome;

    @NotNull(message = "Tipo de projeto é obrigatório")
    private TipoProjeto tipoProjeto;

    private StatusProjeto status = StatusProjeto.ORCAMENTO;

    private LocalDate dataInicio;

    private LocalDate dataPrevista;

    private LocalDate dataFinalizacao;

    @NotNull(message = "Valor total é obrigatório")
    @DecimalMin(value = "0.0", inclusive = false, message = "Valor total deve ser maior que zero")
    private BigDecimal valorTotal = BigDecimal.ZERO;

    @NotNull(message = "Valor da mão de obra é obrigatório")
    @DecimalMin(value = "0.0", inclusive = true, message = "Valor da mão de obra não pode ser negativo")
    private BigDecimal valorMaoObra = BigDecimal.ZERO;

    @NotNull(message = "Margem de lucro é obrigatória")
    @DecimalMin(value = "0.0", inclusive = true, message = "Margem de lucro não pode ser negativa")
    @DecimalMax(value = "100.0", inclusive = true, message = "Margem de lucro não pode ser maior que 100%")
    private BigDecimal margemLucro = new BigDecimal("20.00");

    private String observacoes;

    private List<PecaDTO> pecas; // Alterado de MedidasProjetoDTO para List<PecaDTO>

    private List<ProjetoItemDTO> itens;

    private LocalDate dataCriacao;

    private LocalDate dataAtualizacao;

    @NotNull(message = "Usuário de criação é obrigatório")
    private Integer usuarioCriacao;

}
