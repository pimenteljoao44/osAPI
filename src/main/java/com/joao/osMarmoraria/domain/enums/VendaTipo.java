package com.joao.osMarmoraria.domain.enums;

public enum VendaTipo {
    ORCAMENTO(1,"Orçamento"),
    VENDA(0,"Venda");

    private Integer cod;
    private String descricao;

    VendaTipo(Integer cod, String descricao) {
        this.cod = cod;
        this.descricao = descricao;
    }

    public Integer getCod() {
        return cod;
    }

    public String getDescricao() {
        return descricao;
    }

    public static VendaTipo toEnum(Integer cod) {
        if(cod == null) {
            return null;
        }

        for(VendaTipo vt : VendaTipo.values()) {
            if(cod.equals(vt.getCod())) {
                return vt;
            }
        }
        throw new IllegalArgumentException("Tipo da Venda inválido! "+cod);
    }
}
