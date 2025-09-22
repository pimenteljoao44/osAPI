package com.joao.osMarmoraria.domain.enums;

public enum TipoMovimentacao {
    ENTRADA_COMPRA(1, "Entrada por Compra"),
    ENTRADA_AJUSTE(2, "Entrada por Ajuste"),
    ENTRADA_DEVOLUCAO(3, "Entrada por Devolução"),
    SAIDA_VENDA(4, "Saída por Venda"),
    SAIDA_AJUSTE(5, "Saída por Ajuste"),
    SAIDA_PERDA(6, "Saída por Perda"),
    RESERVA_VENDA(7, "Reserva para Venda"),
    LIBERACAO_RESERVA(8, "Liberação de Reserva"),
    BAIXA_ORDEM_SERVICO(9, "Baixa por Ordem de Serviço"),
    TRANSFERENCIA_ENTRADA(10, "Transferência - Entrada"),
    TRANSFERENCIA_SAIDA(11, "Transferência - Saída");

    private final Integer codigo;
    private final String descricao;

    TipoMovimentacao(Integer codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }

    public Integer getCodigo() {
        return codigo;
    }

    public String getDescricao() {
        return descricao;
    }

    public static TipoMovimentacao toEnum(Integer codigo) {
        if (codigo == null) {
            return null;
        }

        for (TipoMovimentacao tipo : TipoMovimentacao.values()) {
            if (codigo.equals(tipo.getCodigo())) {
                return tipo;
            }
        }

        throw new IllegalArgumentException("Código inválido para TipoMovimentacao: " + codigo);
    }

    public boolean isEntrada() {
        return this == ENTRADA_COMPRA || this == ENTRADA_AJUSTE ||
                this == ENTRADA_DEVOLUCAO || this == TRANSFERENCIA_ENTRADA ||
                this == LIBERACAO_RESERVA;
    }

    public boolean isSaida() {
        return this == SAIDA_VENDA || this == SAIDA_AJUSTE ||
                this == SAIDA_PERDA || this == TRANSFERENCIA_SAIDA ||
                this == BAIXA_ORDEM_SERVICO;
    }

    public boolean isReserva() {
        return this == RESERVA_VENDA;
    }
}
