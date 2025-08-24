package com.joao.osMarmoraria.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.joao.osMarmoraria.domain.Cliente;
import com.joao.osMarmoraria.domain.enums.StatusProjeto;
import com.joao.osMarmoraria.domain.enums.TipoProjeto;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "projetos")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Projeto implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nome", nullable = false, length = 100)
    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    private String nome;

    @Column(name = "descricao", length = 500)
    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    private String descricao;

    @JsonBackReference("cliente-projetos")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    @NotNull(message = "Cliente é obrigatório")
    private Cliente cliente;

    @Column(name = "tipo_projeto", nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Tipo de projeto é obrigatório")
    private TipoProjeto tipoProjeto;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Status é obrigatório")
    private StatusProjeto status = StatusProjeto.ORCAMENTO;

    @Column(name = "data_inicio")
    private LocalDate dataInicio;

    @Column(name = "data_prevista")
    private LocalDate dataPrevista;

    @Column(name = "data_finalizacao")
    private LocalDate dataFinalizacao;

    @Column(name = "valor_total", precision = 10, scale = 2, nullable = false)
    @DecimalMin(value = "0.01", message = "Valor total deve ser maior que zero")
    private BigDecimal valorTotal = BigDecimal.ZERO;

    @Column(name = "valor_mao_obra", precision = 10, scale = 2, nullable = false)
    @NotNull(message = "Valor da mão de obra é obrigatório")
    @DecimalMin(value = "0.0", inclusive = true, message = "Valor da mão de obra não pode ser negativo")
    private BigDecimal valorMaoObra = BigDecimal.ZERO;

    @Column(name = "margem_lucro", precision = 5, scale = 2, nullable = false)
    @NotNull(message = "Margem de lucro é obrigatória")
    @DecimalMin(value = "0.0", inclusive = true, message = "Margem de lucro não pode ser negativa")
    @DecimalMax(value = "100.0", inclusive = true, message = "Margem de lucro não pode ser maior que 100%")
    private BigDecimal margemLucro = new BigDecimal("20.00");

    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;

    // Medidas do projeto
    @Column(name = "profundidade", precision = 8, scale = 2)
    @DecimalMin(value = "0.01", message = "Profundidade deve ser maior que zero")
    private BigDecimal profundidade;

    @Column(name = "largura", precision = 8, scale = 2)
    @DecimalMin(value = "0.01", message = "Largura deve ser maior que zero")
    private BigDecimal largura;

    @Column(name = "altura", precision = 8, scale = 2)
    @DecimalMin(value = "0.01", message = "Altura deve ser maior que zero")
    private BigDecimal altura;

    @Column(name = "area", precision = 10, scale = 4)
    private BigDecimal area;

    @Column(name = "perimetro", precision = 10, scale = 4)
    private BigDecimal perimetro;

    @Column(name = "observacoes_medidas", length = 500)
    private String observacoesMedidas;

    @JsonManagedReference("projeto-itens")
    @OneToMany(mappedBy = "projeto", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProjetoItem> itens = new ArrayList<>();

    @JsonManagedReference("projeto-ordemservico")
    @OneToOne(mappedBy = "projeto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private OrdemServico ordemServico;

    // Auditoria
    @Column(name = "data_criacao", nullable = false)
    private LocalDate dataCriacao;

    @Column(name = "data_atualizacao", nullable = false)
    private LocalDate dataAtualizacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_criacao", nullable = false)
    private Usuario usuarioCriacao;

    // Construtores
    public Projeto() {
    }

    public Projeto(String nome, Cliente cliente, TipoProjeto tipoProjeto) {
        this.nome = nome;
        this.cliente = cliente;
        this.tipoProjeto = tipoProjeto;
        this.dataCriacao = LocalDate.now();
        this.dataAtualizacao = LocalDate.now();
    }

    // Métodos de negócio
    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDate.now();
        dataAtualizacao = LocalDate.now();
        if (status == null) {
            status = StatusProjeto.ORCAMENTO;
        }
        calcularMedidas();
    }

    @PreUpdate
    protected void onUpdate() {
        dataAtualizacao = LocalDate.now();
        calcularMedidas();
    }

    public void calcularMedidas() {
        if (profundidade != null && largura != null) {
            area = profundidade.multiply(largura);
            perimetro = profundidade.add(largura).multiply(new BigDecimal("2"));
        }
    }

    public BigDecimal calcularValorMateriais() {
        return itens.stream()
                .map(ProjetoItem::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void recalcularValorTotal() {
        BigDecimal valorMateriais = calcularValorMateriais();
        BigDecimal subtotal = valorMateriais.add(valorMaoObra);
        BigDecimal multiplicadorLucro = BigDecimal.ONE.add(margemLucro.divide(new BigDecimal("100")));
        valorTotal = subtotal.multiply(multiplicadorLucro);
    }

    public boolean podeGerarOrdemServico() {
        return status == StatusProjeto.ORCAMENTO || status == StatusProjeto.APROVADO || status == StatusProjeto.VENDIDO;
    }

    public boolean podeSerCancelado() {
        return status != StatusProjeto.ENTREGUE && status != StatusProjeto.CANCELADO;
    }

    // Getters e Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public TipoProjeto getTipoProjeto() {
        return tipoProjeto;
    }

    public void setTipoProjeto(TipoProjeto tipoProjeto) {
        this.tipoProjeto = tipoProjeto;
    }

    public StatusProjeto getStatus() {
        return status;
    }

    public void setStatus(StatusProjeto status) {
        this.status = status;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDate getDataPrevista() {
        return dataPrevista;
    }

    public void setDataPrevista(LocalDate dataPrevista) {
        this.dataPrevista = dataPrevista;
    }

    public LocalDate getDataFinalizacao() {
        return dataFinalizacao;
    }

    public void setDataFinalizacao(LocalDate dataFinalizacao) {
        this.dataFinalizacao = dataFinalizacao;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public BigDecimal getValorMaoObra() {
        return valorMaoObra;
    }

    public void setValorMaoObra(BigDecimal valorMaoObra) {
        this.valorMaoObra = valorMaoObra;
    }

    public BigDecimal getMargemLucro() {
        return margemLucro;
    }

    public void setMargemLucro(BigDecimal margemLucro) {
        this.margemLucro = margemLucro;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public BigDecimal getProfundidade() {
        return profundidade;
    }

    public void setProfundidade(BigDecimal profundidade) {
        this.profundidade = profundidade;
    }

    public BigDecimal getLargura() {
        return largura;
    }

    public void setLargura(BigDecimal largura) {
        this.largura = largura;
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

    public String getObservacoesMedidas() {
        return observacoesMedidas;
    }

    public void setObservacoesMedidas(String observacoesMedidas) {
        this.observacoesMedidas = observacoesMedidas;
    }

    public List<ProjetoItem> getItens() {
        return itens;
    }

    public void setItens(List<ProjetoItem> itens) {
        this.itens = itens;
    }

    public OrdemServico getOrdemServico() {
        return ordemServico;
    }

    public void setOrdemServico(OrdemServico ordemServico) {
        this.ordemServico = ordemServico;
    }

    public LocalDate getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDate dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public LocalDate getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(LocalDate dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }

    public Usuario getUsuarioCriacao() {
        return usuarioCriacao;
    }

    public void setUsuarioCriacao(Usuario usuarioCriacao) {
        this.usuarioCriacao = usuarioCriacao;
    }
}