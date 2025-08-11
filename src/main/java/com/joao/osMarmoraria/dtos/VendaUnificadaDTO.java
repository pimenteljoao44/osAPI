package com.joao.osMarmoraria.dtos;

import com.joao.osMarmoraria.domain.Venda;
import com.joao.osMarmoraria.domain.enums.FormaPagamento;
import com.joao.osMarmoraria.domain.enums.VendaTipo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class VendaUnificadaDTO {
    private Integer id;
    private Date dataAbertura;
    private Date dataFechamento;
    private BigDecimal total;
    private BigDecimal desconto;
    private VendaTipo vendaTipo;
    private FormaPagamento formaPagamento;
    private Integer numeroParcelas;
    private String observacoes;

    // Dados do cliente
    private Integer clienteId;
    private String nomeCliente;
    private String emailCliente;
    private String telefoneCliente;

    // Dados do projeto (quando aplicável)
    private Integer projetoId;
    private String nomeProjeto;
    private String tipoProjeto;
    private String statusProjeto;

    // Itens da venda (para vendas de produto)
    private List<ItemVendaDTO> itens;

    // Flags de controle
    private boolean isVendaProjeto;
    private boolean isVendaProduto;
    private boolean isEfetivada;
    private boolean contaReceberGerada;
    private boolean ordemServicoGerada;

    // Valores calculados
    private BigDecimal valorParcela;
    private BigDecimal valorFinal;

    public VendaUnificadaDTO() {}

    public VendaUnificadaDTO(Venda venda) {
        this.id = venda.getVenId();
        this.dataAbertura = venda.getDataAbertura();
        this.dataFechamento = venda.getDataFechamento();
        this.total = venda.getTotal();
        this.desconto = venda.getDesconto();
        this.vendaTipo = venda.getVendaTipo();
        this.formaPagamento = venda.getFormaPagamento();
        this.numeroParcelas = venda.getNumeroParcelas();
        this.observacoes = venda.getObservacoes();

        // Dados do cliente
        if (venda.getCliente() != null) {
            this.clienteId = venda.getCliente().getCliId();
            this.nomeCliente = venda.getCliente().getPessoa().getNome();
            this.telefoneCliente = venda.getCliente().getPessoa().getTelefone();
        }

        // Dados do projeto
        this.projetoId = venda.getProjetoId();
        if (venda.getProjeto() != null) {
            this.nomeProjeto = venda.getProjeto().getNome();
            this.tipoProjeto = venda.getProjeto().getTipoProjeto() != null ?
                    venda.getProjeto().getTipoProjeto().toString() : null;
            this.statusProjeto = venda.getProjeto().getStatus() != null ?
                    venda.getProjeto().getStatus().toString() : null;
        }

        // Itens da venda
        if (venda.getItensVenda() != null && !venda.getItensVenda().isEmpty()) {
            this.itens = venda.getItensVenda().stream()
                    .map(ItemVendaDTO::new)
                    .collect(Collectors.toList());
        }

        // Flags de controle
        this.isVendaProjeto = venda.isVendaProjeto();
        this.isVendaProduto = venda.isVendaProduto();
        this.isEfetivada = venda.getDataFechamento() != null;

        // Verificar se conta a receber foi gerada
        this.contaReceberGerada = venda.getContasReceber() != null &&
                !venda.getContasReceber().isEmpty();

        // Para O.S., verificar se existe projeto com O.S. gerada
        this.ordemServicoGerada = false;
        if (venda.getProjeto() != null && venda.getProjeto().getOrdemServico() != null) {
            this.ordemServicoGerada = true;
        }

        // Valores calculados
        this.valorParcela = venda.getValorParcela();
        this.valorFinal = this.total != null && this.desconto != null ?
                this.total.subtract(this.desconto) : this.total;
    }

    // Métodos auxiliares
    public String getTipoVendaDescricao() {
        if (isVendaProjeto) {
            return "Projeto";
        } else if (isVendaProduto) {
            return "Produto";
        }
        return "Indefinido";
    }

    public String getStatusDescricao() {
        if (isEfetivada) {
            return "Efetivada";
        } else if (vendaTipo == VendaTipo.ORCAMENTO) {
            return "Orçamento";
        }
        return "Pendente";
    }

    public boolean podeEfetivar() {
        return !isEfetivada && (isVendaProjeto || isVendaProduto);
    }

    public boolean podeGerarContaReceber() {
        return isEfetivada && !contaReceberGerada;
    }

    public boolean podeGerarOrdemServico() {
        return isEfetivada && isVendaProjeto && !ordemServicoGerada;
    }

    public boolean podeEditar() {
        return !isEfetivada;
    }
}

