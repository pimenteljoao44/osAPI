package com.joao.osMarmoraria.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.joao.osMarmoraria.domain.enums.UnidadeDeMedida;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "projeto_medidas")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ProjetoMedida implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "projeto_id", nullable = false)
    @NotNull(message = "Projeto é obrigatório")
    private Integer projetoId;

    @JsonBackReference("projeto-medidas")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projeto_id", insertable = false, updatable = false)
    private Projeto projeto;

    @Column(name = "nome", length = 100, nullable = false)
    @NotBlank(message = "Nome da medida é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    private String nome;

    @Column(name = "largura", precision = 10, scale = 4)
    @DecimalMin(value = "0.0", message = "Largura deve ser maior ou igual a zero")
    private BigDecimal largura;

    @Column(name = "altura", precision = 10, scale = 4)
    @DecimalMin(value = "0.0", message = "Altura deve ser maior ou igual a zero")
    private BigDecimal altura;

    @Column(name = "profundidade", precision = 10, scale = 4)
    @DecimalMin(value = "0.0", message = "Profundidade deve ser maior ou igual a zero")
    private BigDecimal profundidade;

    @Column(name = "espessura", precision = 10, scale = 4)
    @DecimalMin(value = "0.0", message = "Espessura deve ser maior ou igual a zero")
    private BigDecimal espessura;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidade_medida", length = 20, nullable = false)
    @NotNull(message = "Unidade de medida é obrigatória")
    private UnidadeDeMedida unidadeMedida = UnidadeDeMedida.METROS;

    @Column(name = "area_calculada", precision = 10, scale = 4)
    private BigDecimal areaCalculada;

    @Column(name = "volume_calculado", precision = 10, scale = 4)
    private BigDecimal volumeCalculado;

    @Column(name = "observacoes", length = 500)
    @Size(max = 500, message = "Observações devem ter no máximo 500 caracteres")
    private String observacoes;

    @Column(name = "coordenada_x", precision = 10, scale = 4)
    private BigDecimal coordenadaX;

    @Column(name = "coordenada_y", precision = 10, scale = 4)
    private BigDecimal coordenadaY;

    @Column(name = "rotacao", precision = 5, scale = 2)
    private BigDecimal rotacao = BigDecimal.ZERO;

    // Construtores
    public ProjetoMedida() {
    }

    public ProjetoMedida(Integer projetoId, String nome, UnidadeDeMedida unidadeMedida) {
        this.projetoId = projetoId;
        this.nome = nome;
        this.unidadeMedida = unidadeMedida != null ? unidadeMedida : UnidadeDeMedida.METROS;
    }

    // Métodos de negócio
    @PrePersist
    @PreUpdate
    protected void calcularDimensoes() {
        calcularArea();
        calcularVolume();
    }

    public void calcularArea() {
        if (largura != null && altura != null) {
            areaCalculada = largura.multiply(altura);
        }
    }

    public void calcularVolume() {
        if (largura != null && altura != null && profundidade != null) {
            volumeCalculado = largura.multiply(altura).multiply(profundidade);
        }
    }

    /**
     * Converte todas as medidas para uma nova unidade
     */
    public void converterParaUnidade(UnidadeDeMedida novaUnidade) {
        if (novaUnidade == null || novaUnidade == this.unidadeMedida) {
            return;
        }

        if (largura != null) {
            largura = UnidadeDeMedida.converter(largura, this.unidadeMedida, novaUnidade);
        }
        if (altura != null) {
            altura = UnidadeDeMedida.converter(altura, this.unidadeMedida, novaUnidade);
        }
        if (profundidade != null) {
            profundidade = UnidadeDeMedida.converter(profundidade, this.unidadeMedida, novaUnidade);
        }
        if (espessura != null) {
            espessura = UnidadeDeMedida.converter(espessura, this.unidadeMedida, novaUnidade);
        }

        this.unidadeMedida = novaUnidade;
        calcularDimensoes();
    }

    /**
     * Retorna a área em metros quadrados (unidade padrão)
     */
    public BigDecimal getAreaEmMetrosQuadrados() {
        if (areaCalculada == null || unidadeMedida.getTipo() != UnidadeDeMedida.TipoUnidade.COMPRIMENTO) {
            return BigDecimal.ZERO;
        }

        double fator = unidadeMedida.getFatorConversao();
        BigDecimal fatorConversao = BigDecimal.valueOf(1.0 / (fator * fator));
        return areaCalculada.multiply(fatorConversao);
    }

    /**
     * Retorna o volume em metros cúbicos (unidade padrão)
     */
    public BigDecimal getVolumeEmMetrosCubicos() {
        if (volumeCalculado == null || unidadeMedida.getTipo() != UnidadeDeMedida.TipoUnidade.COMPRIMENTO) {
            return BigDecimal.ZERO;
        }

        double fator = unidadeMedida.getFatorConversao();
        BigDecimal fatorConversao = BigDecimal.valueOf(1.0 / Math.pow(fator, 3));
        return volumeCalculado.multiply(fatorConversao);
    }

    /**
     * Formata as dimensões com a unidade
     */
    public String formatarDimensoes() {
        StringBuilder sb = new StringBuilder();

        if (largura != null) {
            sb.append("L: ").append(unidadeMedida.formatarComUnidade(largura));
        }
        if (altura != null) {
            if (sb.length() > 0) sb.append(" × ");
            sb.append("A: ").append(unidadeMedida.formatarComUnidade(altura));
        }
        if (profundidade != null) {
            if (sb.length() > 0) sb.append(" × ");
            sb.append("P: ").append(unidadeMedida.formatarComUnidade(profundidade));
        }

        return sb.toString();
    }

    // Getters e Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getProjetoId() {
        return projetoId;
    }

    public void setProjetoId(Integer projetoId) {
        this.projetoId = projetoId;
    }

    public Projeto getProjeto() {
        return projeto;
    }

    public void setProjeto(Projeto projeto) {
        this.projeto = projeto;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public BigDecimal getLargura() {
        return largura;
    }

    public void setLargura(BigDecimal largura) {
        this.largura = largura;
        calcularDimensoes();
    }

    public BigDecimal getAltura() {
        return altura;
    }

    public void setAltura(BigDecimal altura) {
        this.altura = altura;
        calcularDimensoes();
    }

    public BigDecimal getProfundidade() {
        return profundidade;
    }

    public void setProfundidade(BigDecimal profundidade) {
        this.profundidade = profundidade;
        calcularDimensoes();
    }

    public BigDecimal getEspessura() {
        return espessura;
    }

    public void setEspessura(BigDecimal espessura) {
        this.espessura = espessura;
    }

    public UnidadeDeMedida getUnidadeMedida() {
        return unidadeMedida;
    }

    public void setUnidadeMedida(UnidadeDeMedida unidadeMedida) {
        this.unidadeMedida = unidadeMedida != null ? unidadeMedida : UnidadeDeMedida.METROS;
    }

    public BigDecimal getAreaCalculada() {
        return areaCalculada;
    }

    public void setAreaCalculada(BigDecimal areaCalculada) {
        this.areaCalculada = areaCalculada;
    }

    public BigDecimal getVolumeCalculado() {
        return volumeCalculado;
    }

    public void setVolumeCalculado(BigDecimal volumeCalculado) {
        this.volumeCalculado = volumeCalculado;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public BigDecimal getCoordenadaX() {
        return coordenadaX;
    }

    public void setCoordenadaX(BigDecimal coordenadaX) {
        this.coordenadaX = coordenadaX;
    }

    public BigDecimal getCoordenadaY() {
        return coordenadaY;
    }

    public void setCoordenadaY(BigDecimal coordenadaY) {
        this.coordenadaY = coordenadaY;
    }

    public BigDecimal getRotacao() {
        return rotacao;
    }

    public void setRotacao(BigDecimal rotacao) {
        this.rotacao = rotacao != null ? rotacao : BigDecimal.ZERO;
    }
}
