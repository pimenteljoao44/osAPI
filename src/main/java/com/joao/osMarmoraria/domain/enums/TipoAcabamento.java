package com.joao.osMarmoraria.domain.enums;

public enum TipoAcabamento {
    BORDA_RETA("Borda Reta"),
    BORDA_REDONDA("Borda Redonda"),
    BORDA_CHANFRADA("Borda Chanfrada"),
    POLIMENTO("Polimento"),
    FURO_TORNEIRA("Furo para Torneira"),
    FURO_CUBA("Furo para Cuba"),
    RECORTE_PERSONALIZADO("Recorte Personalizado");

    private final String descricao;

    TipoAcabamento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
