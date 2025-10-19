package com.joao.osMarmoraria.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrcamentoPDFDTO {

    private Integer projetoId;

    // Dados do cliente
    private String clienteNome;
    private String clienteEmail;
    private String clienteTelefone;
    private String clienteEndereco;

    // Dados do projeto/orçamento
    private String projetoNome;
    private String projetoDescricao;
    private LocalDate dataOrcamento;
    private LocalDate dataValidade;

    // Medidas
    private BigDecimal largura;
    private BigDecimal comprimento;
    private BigDecimal area;
    private BigDecimal espessura;

    // Valores
    private BigDecimal valorMateriais;
    private BigDecimal valorMaoObra;
    private BigDecimal valorTotal;

    // Observações
    private String observacoes;

    // Itens do orçamento
    private List<ItemOrcamentoPDFDTO> itens;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemOrcamentoPDFDTO {
        private String nome;
        private String descricao;
        private BigDecimal quantidade;
        private String unidade;
        private BigDecimal valorUnitario;
        private BigDecimal valorTotal;
    }
}
