package com.joao.osMarmoraria.dtos;

import javax.validation.constraints.DecimalMin;
import java.math.BigDecimal;

public class MedidasProjetoDTO {

    @DecimalMin(value = "0.01", message = "Profundidade deve ser maior que zero")
    private BigDecimal profundidade;

    @DecimalMin(value = "0.01", message = "Largura deve ser maior que zero")
    private BigDecimal largura;

    @DecimalMin(value = "0.01", message = "Altura deve ser maior que zero")
    private BigDecimal altura;

    private BigDecimal area;

    private BigDecimal perimetro;

    private String observacoes;

    // Construtores
    public MedidasProjetoDTO() {
    }

    public MedidasProjetoDTO(BigDecimal profundidade, BigDecimal largura, BigDecimal altura) {
        this.profundidade = profundidade;
        this.largura = largura;
        this.altura = altura;
        calcularArea();
        calcularPerimetro();
    }

    // Métodos de cálculo
    public void calcularArea() {
        if (profundidade != null && largura != null) {
            area = profundidade.multiply(largura);
        }
    }

    public void calcularPerimetro() {
        if (profundidade != null && largura != null) {
            perimetro = profundidade.add(largura).multiply(new BigDecimal("2"));
        }
    }

    // Getters e Setters
    public BigDecimal getProfundidade() {
        return profundidade;
    }

    public void setProfundidade(BigDecimal profundidade) {
        this.profundidade = profundidade;
        calcularArea();
        calcularPerimetro();
    }

    public BigDecimal getLargura() {
        return largura;
    }

    public void setLargura(BigDecimal largura) {
        this.largura = largura;
        calcularArea();
        calcularPerimetro();
    }

    public BigDecimal getAltura() {
        return altura;
    }

    public void setAltura(BigDecimal altura) {
        this.altura = altura;
    }

    public BigDecimal getArea() {
        return area;
    }

    public void setArea(BigDecimal area) {
        this.area = area;
    }

    public BigDecimal getPerimetro() {
        return perimetro;
    }

    public void setPerimetro(BigDecimal perimetro) {
        this.perimetro = perimetro;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}

