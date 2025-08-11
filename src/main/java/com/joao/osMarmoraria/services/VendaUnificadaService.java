package com.joao.osMarmoraria.services;

import com.joao.osMarmoraria.domain.*;
import com.joao.osMarmoraria.domain.enums.StatusOrdemServico;
import com.joao.osMarmoraria.domain.enums.StatusProjeto;
import com.joao.osMarmoraria.domain.enums.VendaTipo;
import com.joao.osMarmoraria.dtos.*;
import com.joao.osMarmoraria.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class VendaUnificadaService {

    @Autowired
    private VendaRepository vendaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ProjetoRepository projetoRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private ItemVendaRepository itemVendaRepository;

    @Autowired
    private ContaReceberService contaReceberService;

    @Autowired
    private OrdemServicoService ordemServicoService;

    @Autowired
    private ContaReceberRepository contaReceberRepository;

    @Autowired
    private ParcelaRepository parcelaRepository;

    // ========== OPERAÇÕES CRUD ==========

    @Transactional
    public VendaUnificadaDTO criarVenda(VendaUnificadaCreateDTO createDTO) {
        // Validações
        if (!createDTO.isValid()) {
            throw new IllegalArgumentException("Dados inválidos para criação da venda");
        }

        Cliente cliente = clienteRepository.findById(createDTO.getClienteId())
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));

        Venda venda = new Venda();
        venda.setCliente(cliente);
        venda.setVendaTipo(createDTO.getVendaTipo());
        venda.setFormaPagamento(createDTO.getFormaPagamento());
        venda.setDesconto(createDTO.getDesconto());
        venda.setNumeroParcelas(createDTO.getNumeroParcelas());
        venda.setObservacoes(createDTO.getObservacoes());
        venda.setDataAbertura(new Date());

        if (createDTO.isVendaProjeto()) {
            // Venda de projeto
            Projeto projeto = projetoRepository.findById(createDTO.getProjetoId())
                    .orElseThrow(() -> new IllegalArgumentException("Projeto não encontrado"));

            if (projeto.getStatus() != StatusProjeto.APROVADO) {
                throw new IllegalStateException("Projeto deve estar aprovado para gerar venda");
            }

            // Verificar se já existe venda para este projeto
            if (vendaRepository.existsByProjetoId(projeto.getId())) {
                throw new IllegalStateException("Já existe venda para este projeto");
            }

            venda.setProjetoId(projeto.getId());
            venda.setTotal(projeto.getValorTotal());

        } else if (createDTO.isVendaProduto()) {
            // Venda de produtos
            List<ItemVenda> itens = new ArrayList<>();
            BigDecimal totalVenda = BigDecimal.ZERO;

            for (ItemVendaCreateDTO itemDTO : createDTO.getItens()) {
                Produto produto = produtoRepository.findById(itemDTO.getProdutoId())
                        .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado: " + itemDTO.getProdutoId()));

                // Verificar estoque
                if (produto.getQuantidade().compareTo(itemDTO.getQuantidade()) < 0) {
                    throw new IllegalStateException("Estoque insuficiente para o produto: " + produto.getNome());
                }

                ItemVenda item = new ItemVenda();
                item.setProduto(produto);
                item.setQuantidade(itemDTO.getQuantidade());
                item.setPreco(itemDTO.getPreco() != null ? itemDTO.getPreco() : produto.getPrecoVenda());
                item.setVenda(venda);

                itens.add(item);
                totalVenda = totalVenda.add(item.getPreco().multiply(item.getQuantidade()));
            }

            venda.setItensVenda(itens);
            venda.setTotal(totalVenda);
        }

        venda.calculaTotal(); // Aplicar desconto
        venda = vendaRepository.save(venda);

        return new VendaUnificadaDTO(venda);
    }

    @Transactional(readOnly = true)
    public VendaUnificadaDTO buscarPorId(Integer id) {
        Venda venda = vendaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venda não encontrada"));
        return new VendaUnificadaDTO(venda);
    }

    @Transactional(readOnly = true)
    public List<VendaUnificadaDTO> listarVendas(String tipo, Integer clienteId, String status) {
        List<Venda> vendas;

        if (tipo != null || clienteId != null || status != null) {
            // Aplicar filtros
            vendas = vendaRepository.findAll().stream()
                    .filter(v -> tipo == null || v.getVendaTipo().toString().equalsIgnoreCase(tipo))
                    .filter(v -> clienteId == null || v.getCliente().getCliId().equals(clienteId))
                    .filter(v -> status == null ||
                            (status.equalsIgnoreCase("efetivada") && v.getDataFechamento() != null) ||
                            (status.equalsIgnoreCase("pendente") && v.getDataFechamento() == null))
                    .collect(Collectors.toList());
        } else {
            vendas = vendaRepository.findAll();
        }

        return vendas.stream()
                .map(VendaUnificadaDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public VendaUnificadaDTO atualizarVenda(Integer id, VendaUnificadaUpdateDTO updateDTO) {
        Venda venda = vendaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venda não encontrada"));

        if (venda.getDataFechamento() != null) {
            throw new IllegalStateException("Não é possível alterar venda já efetivada");
        }

        if (updateDTO.getFormaPagamento() != null) {
            venda.setFormaPagamento(updateDTO.getFormaPagamento());
        }
        if (updateDTO.getDesconto() != null) {
            venda.setDesconto(updateDTO.getDesconto());
        }
        if (updateDTO.getNumeroParcelas() != null) {
            venda.setNumeroParcelas(updateDTO.getNumeroParcelas());
        }
        if (updateDTO.getObservacoes() != null) {
            venda.setObservacoes(updateDTO.getObservacoes());
        }

        venda.calculaTotal();
        venda = vendaRepository.save(venda);

        return new VendaUnificadaDTO(venda);
    }

    // ========== OPERAÇÕES ESPECÍFICAS ==========

    @Transactional
    public VendaUnificadaDTO efetivarVenda(Integer id) {
        Venda venda = vendaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venda não encontrada"));

        if (venda.getDataFechamento() != null) {
            throw new IllegalStateException("Venda já foi efetivada");
        }

        venda.efetuarVenda();

        // Para vendas de produto, baixar estoque
        if (venda.isVendaProduto()) {
            for (ItemVenda item : venda.getItensVenda()) {
                item.getProduto().baixarEstoque(item.getQuantidade());
            }
        }

        venda = vendaRepository.save(venda);

        return new VendaUnificadaDTO(venda);
    }

    @Transactional
    public String gerarContaReceber(Integer id) {
        Venda venda = vendaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venda não encontrada"));

        if (venda.getDataFechamento() == null) {
            throw new IllegalStateException("Venda deve estar efetivada para gerar conta a receber");
        }

        if (venda.getContasReceber() != null && !venda.getContasReceber().isEmpty()) {
            throw new IllegalStateException("Conta a receber já foi gerada para esta venda");
        }

        // Verificar se a forma de pagamento permite parcelamento
        boolean permiteParcelamento = venda.getFormaPagamento().permiteParcelamento();
        int numeroParcelas = permiteParcelamento ? venda.getNumeroParcelas() : 1;

        if (numeroParcelas == 1) {
            // Pagamento à vista - uma única conta a receber
            ContaReceber conta = new ContaReceber();
            conta.setVenda(venda);
            conta.setValor(venda.getTotal());
            conta.setDataVencimento(LocalDate.now().plusDays(7)); // 7 dias para pagamento
            conta.setStatus("PENDENTE");
            conta.setDescricao("Venda #" + venda.getVenId() + " - Pagamento à vista");

            contaReceberRepository.save(conta);

        } else {
            // Pagamento parcelado - múltiplas contas a receber
            List<Parcela> parcelas = gerarParcelas(venda.getTotal(), numeroParcelas, LocalDate.now().plusDays(30));

            for (Parcela parcela : parcelas) {
                ContaReceber conta = new ContaReceber();
                conta.setVenda(venda);
                conta.setValor(parcela.getValorParcela());
                conta.setDataVencimento(parcela.getDataVencimento());
                conta.setStatus("PENDENTE");
                conta.setDescricao("Venda #" + venda.getVenId() + " - Parcela " + parcela.getNumeroParcela() + "/" + numeroParcelas);

                conta = contaReceberRepository.save(conta);

                // Associar parcela à conta a receber
                parcela.setContaReceber(conta);
                parcelaRepository.save(parcela);
            }
        }

        return "Conta(s) a receber gerada(s) com sucesso - " + numeroParcelas + " parcela(s)";
    }

    /**
     * Gera as parcelas para uma venda
     */
    private List<Parcela> gerarParcelas(BigDecimal valorTotal, int numeroParcelas, LocalDate dataVencimentoInicial) {
        List<Parcela> parcelas = new ArrayList<>();

        // Calcula o valor base de cada parcela
        BigDecimal valorParcela = valorTotal.divide(BigDecimal.valueOf(numeroParcelas), 2, RoundingMode.DOWN);

        // Calcula o resto para ajustar na última parcela
        BigDecimal valorRestante = valorTotal.subtract(valorParcela.multiply(BigDecimal.valueOf(numeroParcelas)));

        for (int i = 1; i <= numeroParcelas; i++) {
            BigDecimal valorFinal = valorParcela;

            // Adiciona o resto na última parcela
            if (i == numeroParcelas) {
                valorFinal = valorFinal.add(valorRestante);
            }

            LocalDate dataVencimento = dataVencimentoInicial.plusMonths(i - 1);

            Parcela parcela = new Parcela();
            parcela.setNumeroParcela(i);
            parcela.setTotalParcelas(numeroParcelas);
            parcela.setValorParcela(valorFinal);
            parcela.setDataVencimento(dataVencimento);
            parcela.setStatus("PENDENTE");

            parcelas.add(parcela);
        }

        return parcelas;
    }

    /**
     * Processa venda completa: efetiva, gera contas a receber e ordem de serviço
     */
    @Transactional
    public Map<String, Object> processarVendaCompleta(Integer vendaId) {
        Map<String, Object> resultado = new HashMap<>();
        List<String> sucessos = new ArrayList<>();
        List<String> erros = new ArrayList<>();

        try {
            // 1. Efetivar venda
            efetivarVenda(vendaId);
            sucessos.add("Venda efetivada com sucesso");

            // 2. Gerar contas a receber
            try {
                String resultadoContas = gerarContaReceber(vendaId);
                sucessos.add(resultadoContas);
            } catch (Exception e) {
                erros.add("Erro ao gerar contas a receber: " + e.getMessage());
            }

            // 3. Gerar ordem de serviço (se for venda de projeto)
            try {
                Venda venda = vendaRepository.findById(vendaId).orElse(null);
                if (venda != null && venda.isVendaProjeto()) {
                    String resultadoOS = gerarOrdemServico(vendaId);
                    sucessos.add(resultadoOS);
                }
            } catch (Exception e) {
                erros.add("Erro ao gerar ordem de serviço: " + e.getMessage());
            }

        } catch (Exception e) {
            erros.add("Erro ao efetivar venda: " + e.getMessage());
        }

        resultado.put("success", erros.isEmpty());
        resultado.put("sucessos", sucessos);
        resultado.put("erros", erros);
        resultado.put("message", erros.isEmpty() ?
                "Processamento completo realizado com sucesso" :
                "Processamento realizado com alguns erros");

        return resultado;
    }


    @Transactional
    public String gerarOrdemServico(Integer id) {
        Venda venda = vendaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venda não encontrada"));

        if (!venda.isVendaProjeto()) {
            throw new IllegalStateException("Ordem de serviço só pode ser gerada para vendas de projeto");
        }

        if (venda.getDataFechamento() == null) {
            throw new IllegalStateException("Venda deve estar efetivada para gerar ordem de serviço");
        }

        Projeto projeto = venda.getProjeto();
        if (projeto.getOrdemServico() != null) {
            throw new IllegalStateException("Ordem de serviço já foi gerada para este projeto");
        }

        // Gerar ordem de serviço
        OrdemServico os = new OrdemServico();
        os.setProjeto(projeto);
        os.setStatus(StatusOrdemServico.PENDENTE);
        os.setDataCriacao(LocalDateTime.now());
        os.setObservacoes("Ordem de serviço gerada automaticamente pela venda #" + venda.getVenId());

        ordemServicoService.gerarPorProjeto(os.getId());

        return "Ordem de serviço gerada com sucesso";
    }

    // ========== OPERAÇÕES DE ITENS ==========

    @Transactional
    public VendaUnificadaDTO adicionarItem(Integer vendaId, ItemVendaCreateDTO itemDTO) {
        Venda venda = vendaRepository.findById(vendaId)
                .orElseThrow(() -> new RuntimeException("Venda não encontrada"));

        if (venda.getDataFechamento() != null) {
            throw new IllegalStateException("Não é possível adicionar itens a venda efetivada");
        }

        if (!venda.isVendaProduto()) {
            throw new IllegalStateException("Só é possível adicionar itens a vendas de produto");
        }

        Produto produto = produtoRepository.findById(itemDTO.getProdutoId())
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado"));

        // Verificar se produto já existe na venda
        boolean produtoJaExiste = venda.getItensVenda().stream()
                .anyMatch(item -> item.getProduto().getProdId().equals(produto.getProdId()));

        if (produtoJaExiste) {
            throw new IllegalStateException("Produto já está na venda");
        }

        ItemVenda item = new ItemVenda();
        item.setProduto(produto);
        item.setQuantidade(itemDTO.getQuantidade());
        item.setPreco(itemDTO.getPreco() != null ? itemDTO.getPreco() : produto.getPrecoVenda());
        item.setVenda(venda);

        venda.getItensVenda().add(item);
        venda.calculaTotal();

        venda = vendaRepository.save(venda);

        return new VendaUnificadaDTO(venda);
    }

    @Transactional
    public VendaUnificadaDTO removerItem(Integer vendaId, Integer itemId) {
        Venda venda = vendaRepository.findById(vendaId)
                .orElseThrow(() -> new RuntimeException("Venda não encontrada"));

        if (venda.getDataFechamento() != null) {
            throw new IllegalStateException("Não é possível remover itens de venda efetivada");
        }

        ItemVenda item = venda.getItensVenda().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Item não encontrado na venda"));

        venda.getItensVenda().remove(item);
        venda.calculaTotal();

        itemVendaRepository.delete(item);
        venda = vendaRepository.save(venda);

        return new VendaUnificadaDTO(venda);
    }

    @Transactional
    public VendaUnificadaDTO atualizarItem(Integer vendaId, Integer itemId, ItemVendaUpdateDTO itemDTO) {
        Venda venda = vendaRepository.findById(vendaId)
                .orElseThrow(() -> new RuntimeException("Venda não encontrada"));

        if (venda.getDataFechamento() != null) {
            throw new IllegalStateException("Não é possível alterar itens de venda efetivada");
        }

        ItemVenda item = venda.getItensVenda().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Item não encontrado na venda"));

        if (itemDTO.getQuantidade() != null) {
            item.setQuantidade(itemDTO.getQuantidade());
        }
        if (itemDTO.getPreco() != null) {
            item.setPreco(itemDTO.getPreco());
        }

        venda.calculaTotal();
        venda = vendaRepository.save(venda);

        return new VendaUnificadaDTO(venda);
    }

    // ========== CONSULTAS ESPECÍFICAS ==========

    @Transactional(readOnly = true)
    public List<VendaUnificadaDTO> buscarPorCliente(Integer clienteId) {
        List<Venda> vendas = vendaRepository.findByClienteId(clienteId);
        return vendas.stream()
                .map(VendaUnificadaDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VendaUnificadaDTO> listarVendasProdutos() {
        List<Venda> vendas = vendaRepository.findByVendaTipoAndProjetoIdIsNotNull(VendaTipo.VENDA);
        return vendas.stream()
                .map(VendaUnificadaDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VendaUnificadaDTO> listarVendasProjetos() {
        List<Venda> vendas = vendaRepository.findByVendaTipoAndProjetoIdIsNotNull(VendaTipo.ORCAMENTO);
        return vendas.stream()
                .map(VendaUnificadaDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VendaUnificadaDTO> listarOrcamentos() {
        List<Venda> vendas = vendaRepository.findByVendaTipoAndDataFechamentoIsNull(VendaTipo.ORCAMENTO);
        return vendas.stream()
                .map(VendaUnificadaDTO::new)
                .collect(Collectors.toList());
    }

    // ========== DASHBOARD E ESTATÍSTICAS ==========

    @Transactional(readOnly = true)
    public Map<String, Object> obterEstatisticas() {
        Map<String, Object> stats = new HashMap<>();

        List<Venda> todasVendas = vendaRepository.findAll();

        // Estatísticas gerais
        stats.put("totalVendas", todasVendas.size());
        stats.put("vendasEfetivadas", todasVendas.stream()
                .mapToLong(v -> v.getDataFechamento() != null ? 1 : 0).sum());
        stats.put("orcamentosPendentes", todasVendas.stream()
                .mapToLong(v -> v.getVendaTipo() == VendaTipo.ORCAMENTO && v.getDataFechamento() == null ? 1 : 0).sum());

        // Valores
        BigDecimal valorTotalVendas = todasVendas.stream()
                .filter(v -> v.getDataFechamento() != null)
                .map(Venda::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("valorTotalVendas", valorTotalVendas);

        BigDecimal valorOrcamentos = todasVendas.stream()
                .filter(v -> v.getVendaTipo() == VendaTipo.ORCAMENTO && v.getDataFechamento() == null)
                .map(Venda::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("valorOrcamentos", valorOrcamentos);

        // Por tipo
        long vendasProduto = todasVendas.stream()
                .mapToLong(v -> v.isVendaProduto() ? 1 : 0).sum();
        long vendasProjeto = todasVendas.stream()
                .mapToLong(v -> v.isVendaProjeto() ? 1 : 0).sum();

        stats.put("vendasProduto", vendasProduto);
        stats.put("vendasProjeto", vendasProjeto);

        return stats;
    }
}

