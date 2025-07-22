package com.joao.osMarmoraria.
        domain.enums;

public enum StatusConta {
    PENDENTE(0,"PENDENTE"),
    PAGO(1,"PAGO"),
    ENCERRADO(2,"ENCERRADO");

    private Integer cod;
    private String descricao;
    private StatusConta(Integer cod, String descricao) {
        this.cod = cod;
        this.descricao = descricao;
    }

    public Integer getCod() {
        return cod;
    }

    public String getDescricao() {
        return descricao;
    }

    public static StatusConta toEnum(Integer cod) {
        if(cod == null) {
            return null;
        }

        for(StatusConta s : StatusConta.values()) {
            if(cod.equals(	s.getCod())) {
                return s;
            }
        }
        throw new IllegalArgumentException("Status invalido! "+cod);
    }
}
