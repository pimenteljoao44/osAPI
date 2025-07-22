package com.joao.osMarmoraria.domain.enums;

public enum TipoPessoa {
    PESSOA_FISICA("PF"),
    PESSOA_JURIDICA("");
    String descricao;

    TipoPessoa(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
