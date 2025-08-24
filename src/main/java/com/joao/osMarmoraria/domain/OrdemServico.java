package com.joao.osMarmoraria.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.joao.osMarmoraria.domain.enums.StatusOrdemServico;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ordens_servico")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class OrdemServico implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "numero", nullable = false, unique = true, length = 20)
    @NotBlank(message = "Número da O.S. é obrigatório")
    @Size(max = 20, message = "Número deve ter no máximo 20 caracteres")
    private String numero;

    @Column(name = "projeto_id", nullable = false)
    @NotNull(message = "Projeto é obrigatório")
    private Integer projetoId;

    @JsonBackReference("projeto-ordemservico")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projeto_id", insertable = false, updatable = false)
    private Projeto projeto;

    @Column(name = "cliente_id", nullable = false)
    @NotNull(message = "Cliente é obrigatório")
    private Integer clienteId;
    @JsonBackReference("cliente-ordemservico")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", insertable = false, updatable = false)
    private Cliente cliente;

    @Column(name = "data_emissao", nullable = false)
    @NotNull(message = "Data de emissão é obrigatória")
    private LocalDate dataEmissao;

    @Column(name = "data_prevista_inicio")
    private LocalDate dataPrevistaInicio;

    @Column(name = "data_prevista_conclusao")
    private LocalDate dataPrevistaConclusao;

    @Column(name = "data_inicio")
    private LocalDate dataInicio;

    @Column(name = "data_conclusao")
    private LocalDate dataConclusao;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Status é obrigatório")
    private StatusOrdemServico status = StatusOrdemServico.PENDENTE;

    @Column(name = "responsavel", length = 100)
    @Size(max = 100, message = "Responsável deve ter no máximo 100 caracteres")
    private String responsavel;

    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "instrucoes_tecnicas", columnDefinition = "TEXT")
    private String instrucoesTecnicas;

    @Column(name = "valor_total", precision = 10, scale = 2, nullable = false)
    @NotNull(message = "Valor total é obrigatório")
    @DecimalMin(value = "0.0", inclusive = false, message = "Valor total deve ser maior que zero")
    private BigDecimal valorTotal;

    // Relacionamentos
    @OneToMany(mappedBy = "ordemServico", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<ItemOrdemServico> itens = new ArrayList<>();

    // Auditoria
    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_atualizacao", nullable = false)
    private LocalDateTime dataAtualizacao;

    @Column(name = "usuario_criacao", nullable = false)
    @NotNull(message = "Usuário de criação é obrigatório")
    private Integer usuarioCriacao;

    @ManyToOne
    @JoinColumn(name = "funcionario_id")
    private Funcionario funcionario;

    @ManyToOne
    @JoinColumn(name = "servico_id")
    private Servico servico;

    // Construtores
    public OrdemServico() {
    }

    public OrdemServico(String numero, Integer projetoId, Integer clienteId, Integer usuarioCriacao) {
        this.numero = numero;
        this.projetoId = projetoId;
        this.clienteId = clienteId;
        this.usuarioCriacao = usuarioCriacao;
        this.dataEmissao = LocalDate.now();
        this.dataCriacao = LocalDateTime.now();
        this.dataAtualizacao = LocalDateTime.now();
    }

    // Métodos de negócio
    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now();
        dataAtualizacao = LocalDateTime.now();
        if (dataEmissao == null) {
            dataEmissao = LocalDate.now();
        }
        if (status == null) {
            status = StatusOrdemServico.PENDENTE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        dataAtualizacao = LocalDateTime.now();
    }

    public static String gerarNumeroOS() {
        return "OS" + System.currentTimeMillis();
    }

    public void aprovar() {
        if (status == StatusOrdemServico.PENDENTE) {
            status = StatusOrdemServico.APROVADA;
        } else {
            throw new IllegalStateException("Ordem de serviço deve estar pendente para ser aprovada");
        }
    }

    public void agendar(LocalDate dataInicio, LocalDate dataConclusao) {
        if (status == StatusOrdemServico.APROVADA || status == StatusOrdemServico.PENDENTE) {
            this.dataPrevistaInicio = dataInicio;
            this.dataPrevistaConclusao = dataConclusao;
            if (status == StatusOrdemServico.PENDENTE) {
                status = StatusOrdemServico.AGENDADA;
            }
        } else {
            throw new IllegalStateException("Ordem de serviço deve estar aprovada ou pendente para ser agendada");
        }
    }

    public void iniciarExecucao() {
        if (status == StatusOrdemServico.AGENDADA || status == StatusOrdemServico.APROVADA) {
            status = StatusOrdemServico.EM_ANDAMENTO;
            dataInicio = LocalDate.now();
        } else {
            throw new IllegalStateException("Ordem de serviço deve estar agendada ou aprovada para ser iniciada");
        }
    }

    public void pausarExecucao() {
        if (status == StatusOrdemServico.EM_ANDAMENTO) {
            status = StatusOrdemServico.PAUSADA;
        } else {
            throw new IllegalStateException("Ordem de serviço deve estar em andamento para ser pausada");
        }
    }

    public void retornarExecucao() {
        if (status == StatusOrdemServico.PAUSADA) {
            status = StatusOrdemServico.EM_ANDAMENTO;
        } else {
            throw new IllegalStateException("Ordem de serviço deve estar pausada para retornar execução");
        }
    }

    public void concluirExecucao() {
        if (status == StatusOrdemServico.EM_ANDAMENTO || status == StatusOrdemServico.PAUSADA) {
            status = StatusOrdemServico.CONCLUIDA;
            dataConclusao = LocalDate.now();
        } else {
            throw new IllegalStateException("Ordem de serviço deve estar em andamento ou pausada para ser concluída");
        }
    }

    public void cancelarExecucao() {
        if (status != StatusOrdemServico.CONCLUIDA) {
            status = StatusOrdemServico.CANCELADA;
        } else {
            throw new IllegalStateException("Ordem de serviço concluída não pode ser cancelada");
        }
    }

    public boolean podeSerAprovada() {
        return status == StatusOrdemServico.PENDENTE;
    }

    public boolean podeSerAgendada() {
        return status == StatusOrdemServico.APROVADA || status == StatusOrdemServico.PENDENTE;
    }

    public boolean podeSerIniciada() {
        return status == StatusOrdemServico.AGENDADA || status == StatusOrdemServico.APROVADA;
    }

    public boolean podeSerPausada() {
        return status == StatusOrdemServico.EM_ANDAMENTO;
    }

    public boolean podeSerConcluida() {
        return status == StatusOrdemServico.EM_ANDAMENTO || status == StatusOrdemServico.PAUSADA;
    }

    public boolean podeSerCancelada() {
        return status != StatusOrdemServico.CONCLUIDA;
    }

    public Integer getDiasEmAndamento() {
        if (dataInicio == null) return 0;
        LocalDate dataFim = dataConclusao != null ? dataConclusao : LocalDate.now();
        return (int) java.time.temporal.ChronoUnit.DAYS.between(dataInicio, dataFim);
    }

    // Getters e Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
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

    public Integer getClienteId() {
        return clienteId;
    }

    public void setClienteId(Integer clienteId) {
        this.clienteId = clienteId;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public LocalDate getDataEmissao() {
        return dataEmissao;
    }

    public void setDataEmissao(LocalDate dataEmissao) {
        this.dataEmissao = dataEmissao;
    }

    public LocalDate getDataPrevistaInicio() {
        return dataPrevistaInicio;
    }

    public void setDataPrevistaInicio(LocalDate dataPrevistaInicio) {
        this.dataPrevistaInicio = dataPrevistaInicio;
    }

    public LocalDate getDataPrevistaConclusao() {
        return dataPrevistaConclusao;
    }

    public void setDataPrevistaConclusao(LocalDate dataPrevistaConclusao) {
        this.dataPrevistaConclusao = dataPrevistaConclusao;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDate getDataConclusao() {
        return dataConclusao;
    }

    public void setDataConclusao(LocalDate dataConclusao) {
        this.dataConclusao = dataConclusao;
    }

    public StatusOrdemServico getStatus() {
        return status;
    }

    public void setStatus(StatusOrdemServico status) {
        this.status = status;
    }

    public String getResponsavel() {
        return responsavel;
    }

    public void setResponsavel(String responsavel) {
        this.responsavel = responsavel;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public String getInstrucoesTecnicas() {
        return instrucoesTecnicas;
    }

    public void setInstrucoesTecnicas(String instrucoesTecnicas) {
        this.instrucoesTecnicas = instrucoesTecnicas;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public List<ItemOrdemServico> getItens() {
        return itens;
    }

    public void setItens(List<ItemOrdemServico> itens) {
        this.itens = itens;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }

    public Integer getUsuarioCriacao() {
        return usuarioCriacao;
    }

    public void setUsuarioCriacao(Integer usuarioCriacao) {
        this.usuarioCriacao = usuarioCriacao;
    }

    public Funcionario getFuncionario() {
        return funcionario;
    }

    public void setFuncionario(Funcionario funcionario) {
        this.funcionario = funcionario;
    }

    public Servico getServico() {
        return servico;
    }

    public void setServico(Servico servico) {
        this.servico = servico;
    }
}