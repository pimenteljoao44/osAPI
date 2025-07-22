package com.joao.osMarmoraria.domain.enums;

public enum StatusProjeto {
    ORCAMENTO("Orçamento"),
    APROVADO("Aprovado"),
    EM_PRODUCAO("Em Produção"),
    AGUARDANDO_ENTREGA("Aguardando Entrega"),
    ENTREGUE("Entregue"),
    CANCELADO("Cancelado"),
    PRONTO("PRONTO"),
    VENDIDO("Vendido");

    private final String descricao;

    StatusProjeto(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
