package com.joao.osMarmoraria.dtos;

import com.joao.osMarmoraria.domain.enums.FormaPagamento;
import com.joao.osMarmoraria.domain.enums.VendaTipo;
import lombok.Data;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

@Data
public class VendaUnificadaCreateDTO {

    @NotNull(message = "Cliente é obrigatório")
    private Integer clienteId;

    @NotNull(message = "Tipo de venda é obrigatório")
    private VendaTipo vendaTipo;

    @NotNull(message = "Forma de pagamento é obrigatória")
    private FormaPagamento formaPagamento;

    // Para vendas de projeto
    private Integer projetoId;

    // Para vendas de produto
    private List<ItemVendaCreateDTO> itens;

    @DecimalMin(value = "0.0", inclusive = true, message = "Desconto não pode ser negativo")
    private BigDecimal desconto = BigDecimal.ZERO;

    @Min(value = 1, message = "Número de parcelas deve ser maior que zero")
    @Max(value = 60, message = "Número de parcelas não pode exceder 60")
    private Integer numeroParcelas = 1;

    @Size(max = 1000, message = "Observações não podem exceder 1000 caracteres")
    private String observacoes;

    // Validações customizadas
    public boolean isVendaProjeto() {
        return vendaTipo == VendaTipo.ORCAMENTO && projetoId != null;
    }

    public boolean isVendaProduto() {
        return vendaTipo == VendaTipo.VENDA && itens != null && !itens.isEmpty();
    }

    public boolean isValid() {
        if (vendaTipo == VendaTipo.ORCAMENTO) {
            return projetoId != null;
        } else if (vendaTipo == VendaTipo.VENDA) {
            return itens != null && !itens.isEmpty();
        }
        return false;
    }
}

