package com.joao.osMarmoraria.domain.enums;

public enum FormaPagamento {
    DINHEIRO(0,"Dinheiro"),
    PIX(1,"Pix"),
    CARTAO_DE_CREDITO(2,"Cartão de Crédito"),
    CARTAO_DE_DEBITO(3,"Cartão de Débito"),
    BOLETO_BANCARIO(4,"Boleto Bancario"),
    TRANSFERENCIA_BANCARIA(5,""),
    CHEQUE(6,"Cheque");

    private Integer cod;
    private String descricao;

    private FormaPagamento (Integer cod, String descricao) {
        this.cod = cod;
        this.descricao = descricao;
    }

    public Integer getCod() {
        return cod;
    }

    public String getDescricao() {
        return descricao;
    }

    public static FormaPagamento toEnum(Integer cod) {
        if(cod == null) {
            return null;
        }

        for(FormaPagamento fp : FormaPagamento.values()) {
            if(cod.equals(fp.getCod())) {
                return fp;
            }
        }
        throw new IllegalArgumentException("Forma de pagamento invalida! "+cod);
    }
}
