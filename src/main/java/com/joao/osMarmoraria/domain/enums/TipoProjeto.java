package com.joao.osMarmoraria.domain.enums;

public enum TipoProjeto {
    BANHEIRO("Banheiro"),
    CUBA("Cuba"),
    COZINHA("Cozinha"),
    BANCADA("Bancada"),
    PISO("Piso"),
    PAREDE("Parede"),
    ESCADA("Escada"),
    OUTROS("Outros"),
    PIA("Pia"),
    SOLEIRA("Soleira"),
    LAREIRA("Lareira");

    private final String descricao;

    TipoProjeto(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
