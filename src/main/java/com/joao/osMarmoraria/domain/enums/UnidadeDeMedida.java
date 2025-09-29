package com.joao.osMarmoraria.domain.enums;

import java.math.BigDecimal;
import java.math.RoundingMode;

public enum UnidadeDeMedida {
    // Unidades de Comprimento
    METROS("m", "Metros", 1.0, TipoUnidade.COMPRIMENTO),
    CENTIMETROS("cm", "Centímetros", 100.0, TipoUnidade.COMPRIMENTO),
    POLEGADAS("in", "Polegadas", 39.3701, TipoUnidade.COMPRIMENTO),

    // Unidades de Quantidade
    UNIDADE("un", "Unidade", 1.0, TipoUnidade.QUANTIDADE),
    PECA("pç", "Peça", 1.0, TipoUnidade.QUANTIDADE),

    // Unidades de Área
    METRO_QUADRADO("m²", "Metro Quadrado", 1.0, TipoUnidade.AREA),

    // Unidades de Volume
    LITRO("L", "Litro", 1.0, TipoUnidade.VOLUME),

    // Unidades de Peso
    QUILOGRAMA("kg", "Quilograma", 1.0, TipoUnidade.PESO);

    public enum TipoUnidade {
        COMPRIMENTO,
        QUANTIDADE,
        AREA,
        VOLUME,
        PESO
    }

    private final String simbolo;
    private final String descricao;
    private final double fatorConversao;
    private final TipoUnidade tipo;

    UnidadeDeMedida(String simbolo, String descricao, double fatorConversao, TipoUnidade tipo) {
        this.simbolo = simbolo;
        this.descricao = descricao;
        this.fatorConversao = fatorConversao;
        this.tipo = tipo;
    }

    public String getSimbolo() {
        return simbolo;
    }

    public String getDescricao() {
        return descricao;
    }

    public double getFatorConversao() {
        return fatorConversao;
    }

    public TipoUnidade getTipo() {
        return tipo;
    }

    public BigDecimal converterParaUnidadeBase(BigDecimal valor) {
        if (valor == null) return BigDecimal.ZERO;
        if (this.tipo != TipoUnidade.COMPRIMENTO) {
            return valor; 
        }
        BigDecimal fator = BigDecimal.valueOf(1.0 / fatorConversao);
        return valor.multiply(fator).setScale(4, RoundingMode.HALF_UP);
    }

    public BigDecimal converterDaUnidadeBase(BigDecimal valorBase) {
        if (valorBase == null) return BigDecimal.ZERO;
        if (this.tipo != TipoUnidade.COMPRIMENTO) {
            return valorBase;
        }
        BigDecimal fator = BigDecimal.valueOf(fatorConversao);
        return valorBase.multiply(fator).setScale(4, RoundingMode.HALF_UP);
    }

    public static BigDecimal converter(BigDecimal valor, UnidadeDeMedida origem, UnidadeDeMedida destino) {
        if (valor == null || origem == null || destino == null) {
            return BigDecimal.ZERO;
        }

        if (origem == destino) {
            return valor;
        }

        if (origem.getTipo() != destino.getTipo()) {
            throw new IllegalArgumentException("Não é possível converter unidades de tipos diferentes: " + origem.getTipo() + " para " + destino.getTipo());
        }

        if (origem.getTipo() == TipoUnidade.COMPRIMENTO) {
            BigDecimal valorNaBase = origem.converterParaUnidadeBase(valor);
            return destino.converterDaUnidadeBase(valorNaBase);
        }

        return valor;
    }

    public String formatarComUnidade(BigDecimal valor) {
        if (valor == null) return "0 " + simbolo;
        return valor.setScale(2, RoundingMode.HALF_UP) + " " + simbolo;
    }

    public static UnidadeDeMedida getUnidadePadrao() {
        return METROS;
    }

    public static UnidadeDeMedida porSimbolo(String simbolo) {
        if (simbolo == null) return getUnidadePadrao();

        for (UnidadeDeMedida unidade : values()) {
            if (unidade.getSimbolo().equalsIgnoreCase(simbolo)) {
                return unidade;
            }
        }
        return getUnidadePadrao();
    }

    public static boolean isUnidadeValida(String simbolo) {
        if (simbolo == null) return false;

        for (UnidadeDeMedida unidade : values()) {
            if (unidade.getSimbolo().equalsIgnoreCase(simbolo)) {
                return true;
            }
        }
        return false;
    }
}
