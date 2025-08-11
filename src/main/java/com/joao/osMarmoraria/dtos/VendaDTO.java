package com.joao.osMarmoraria.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.joao.osMarmoraria.domain.ItemVenda;
import com.joao.osMarmoraria.domain.Venda;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class VendaDTO implements Serializable {

    private Integer id;
    private Integer vendaId;

    private Date dataAbertura;
    private Date dataVenda;
    private Date dataFechamento;

    private BigDecimal total = BigDecimal.ZERO;
    private BigDecimal valorTotal = BigDecimal.ZERO;
    private BigDecimal desconto = BigDecimal.ZERO;

    private Integer vendaTipo;
    private Integer formaPagamento;

    private List<ItemVendaDTO> itensVenda = new ArrayList<>();

    private Integer cliente;
    private String clienteNome;
    private Integer funcionario;
    private String funcionarioNome;
    private String observacoes;

    // Campos de controle de status
    private Boolean efetivada = false;
    private Boolean contaReceberGerada = false;
    private Boolean ordemServicoGerada = false;
    private String status;

    // Campos para vendas de projeto
    private Integer projetoId;
    private Integer numeroParcelas = 1;
    private Date dataVencimento;
    private List<ParcelaDTO> parcelas;

    // Campo para parcelamento usando InstallmentRequestDTO
    private InstallmentRequestDTO installmentRequest;

    public VendaDTO(Venda obj) {
        this.id = obj.getVenId();
        this.vendaId = obj.getVenId();
        this.cliente = obj.getCliente().getCliId();
        this.clienteNome = obj.getCliente().getPessoa() != null ? obj.getCliente().getPessoa().getNome() : "";
        this.dataAbertura = obj.getDataAbertura();
        this.dataVenda = obj.getDataAbertura();
        this.dataFechamento = obj.getDataFechamento();
        this.vendaTipo = obj.getVendaTipo().getCod();
        this.formaPagamento = obj.getFormaPagamento().getCod();
        this.desconto = obj.getDesconto();
        this.total = obj.calculaTotal();
        this.valorTotal = obj.calculaTotal();
        this.observacoes = obj.getObservacoes();
        this.projetoId = obj.getProjetoId();
        this.numeroParcelas = obj.getNumeroParcelas();

        // Determinar status baseado na data de fechamento
        this.efetivada = obj.getDataFechamento() != null;
        this.status = this.efetivada ? "Efetivada" : "Pendente";

        // Por enquanto, assumir que contas e OS não foram geradas (pode ser melhorado com consultas específicas)
        this.contaReceberGerada = false;
        this.ordemServicoGerada = false;

        this.itensVenda = obj.getItensVenda().stream()
                .map(ItemVendaDTO::new)
                .collect(Collectors.toList());
    }

    public VendaDTO() {
    }

    // Getters e Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getVendaId() {
        return vendaId;
    }

    public void setVendaId(Integer vendaId) {
        this.vendaId = vendaId;
    }

    public Date getDataAbertura() {
        return dataAbertura;
    }

    public void setDataAbertura(Date dataAbertura) {
        this.dataAbertura = dataAbertura;
    }

    public Date getDataVenda() {
        return dataVenda;
    }

    public void setDataVenda(Date dataVenda) {
        this.dataVenda = dataVenda;
    }

    public Date getDataFechamento() {
        return dataFechamento;
    }

    public void setDataFechamento(Date dataFechamento) {
        this.dataFechamento = dataFechamento;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public BigDecimal getDesconto() {
        return desconto;
    }

    public void setDesconto(BigDecimal desconto) {
        this.desconto = desconto;
    }

    public Integer getVendaTipo() {
        return vendaTipo;
    }

    public void setVendaTipo(Integer vendaTipo) {
        this.vendaTipo = vendaTipo;
    }

    public Integer getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(Integer formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public List<ItemVendaDTO> getItensVenda() {
        return itensVenda;
    }

    public void setItensVenda(List<ItemVendaDTO> itensVenda) {
        this.itensVenda = itensVenda;
    }

    public Integer getCliente() {
        return cliente;
    }

    public void setCliente(Integer cliente) {
        this.cliente = cliente;
    }

    public String getClienteNome() {
        return clienteNome;
    }

    public void setClienteNome(String clienteNome) {
        this.clienteNome = clienteNome;
    }

    public Integer getFuncionario() {
        return funcionario;
    }

    public void setFuncionario(Integer funcionario) {
        this.funcionario = funcionario;
    }

    public String getFuncionarioNome() {
        return funcionarioNome;
    }

    public void setFuncionarioNome(String funcionarioNome) {
        this.funcionarioNome = funcionarioNome;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public Boolean getEfetivada() {
        return efetivada;
    }

    public void setEfetivada(Boolean efetivada) {
        this.efetivada = efetivada;
    }

    public Boolean getContaReceberGerada() {
        return contaReceberGerada;
    }

    public void setContaReceberGerada(Boolean contaReceberGerada) {
        this.contaReceberGerada = contaReceberGerada;
    }

    public Boolean getOrdemServicoGerada() {
        return ordemServicoGerada;
    }

    public void setOrdemServicoGerada(Boolean ordemServicoGerada) {
        this.ordemServicoGerada = ordemServicoGerada;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getProjetoId() {
        return projetoId;
    }

    public void setProjetoId(Integer projetoId) {
        this.projetoId = projetoId;
    }

    public Integer getNumeroParcelas() {
        return numeroParcelas;
    }

    public void setNumeroParcelas(Integer numeroParcelas) {
        this.numeroParcelas = numeroParcelas;
    }

    public List<ParcelaDTO> getParcelas() {
        return parcelas;
    }

    public void setParcelas(List<ParcelaDTO> parcelas) {
        this.parcelas = parcelas;
    }

    public Date getDataVencimento() {
        return dataVencimento;
    }

    public void setDataVencimento(Date dataVencimento) {
        this.dataVencimento = dataVencimento;
    }

    public InstallmentRequestDTO getInstallmentRequest() {
        return installmentRequest;
    }

    public void setInstallmentRequest(InstallmentRequestDTO installmentRequest) {
        this.installmentRequest = installmentRequest;
    }
}
