package com.joao.osMarmoraria.services;

import com.joao.osMarmoraria.domain.*;
import com.joao.osMarmoraria.domain.enums.FormaPagamento;
import com.joao.osMarmoraria.domain.enums.StatusProjeto;
import com.joao.osMarmoraria.domain.enums.VendaTipo;
import com.joao.osMarmoraria.dtos.*;
import com.joao.osMarmoraria.repository.*;
import com.joao.osMarmoraria.services.exceptions.ObjectNotFoundException;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
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
public class VendaService {

    @Autowired
    private VendaRepository vendaRepository;

    @Autowired
    private ItemVendaRepository itemVendaRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ProjetoRepository projetoRepository;

    @Autowired
    private OrdemServicoService ordemServicoService;

    @Autowired
    private ParcelaService parcelaService;

    @Autowired
    private ContaReceberRepository contaReceberRepository;

    @Autowired
    private EstoqueService estoqueService;

    @Transactional(readOnly = true)
    public Venda findById(Integer id) {
        return vendaRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Venda não encontrada! ID: " + id));
    }

    public List<Venda> findAll() {
        return vendaRepository.findAll();
    }

    @Transactional
    public Venda create(VendaDTO objDto) {
        if (VendaTipo.toEnum(objDto.getVendaTipo()) == VendaTipo.VENDA &&
                (objDto.getItensVenda() == null || objDto.getItensVenda().isEmpty())) {
            throw new DataIntegrityViolationException("Uma Venda de Produto deve conter pelo menos um item.");
        }

        Venda venda = fromDTO(objDto);

        // Para vendas de produto, o total é calculado. Para projetos, já foi definido no fromDTO.
        if (venda.getVendaTipo() == VendaTipo.VENDA) {
            venda.setTotal(venda.calculaTotal());
        }

        Venda savedVenda = vendaRepository.save(venda);

        if (savedVenda.getVendaTipo() == VendaTipo.VENDA) {
            for (ItemVenda item : savedVenda.getItensVenda()) {
                item.setVenda(savedVenda); // Garante a associação bidirecional
                itemVendaRepository.save(item);
                Produto produto = item.getProduto();
                produto.baixarEstoque(item.getQuantidade());
                produtoRepository.save(produto);
            }
        }

        return savedVenda;
    }

    @Transactional
    public Map<String, Object> processarVendaCompleta(Integer vendaId) {
        Map<String, Object> resultado = new HashMap<>();
        List<String> erros = new ArrayList<>();
        List<String> sucessos = new ArrayList<>();

        try {
            Venda venda = findById(vendaId);

            if (venda.getDataFechamento() == null) {
                venda.efetuarVenda();
                vendaRepository.save(venda);
                sucessos.add("Venda efetivada com sucesso");
            }

            try {
                // A chamada agora usa o novo método corrigido que gera uma conta por parcela
                gerarContasReceberParceladas(venda.getVenId());
                sucessos.add("Contas a receber e parcelas geradas com sucesso");
            } catch (Exception e) {
                if (e.getMessage().contains("já foram geradas")) {
                    sucessos.add("Contas a receber já existentes para esta venda.");
                } else {
                    erros.add("Falha ao gerar contas a receber: " + e.getMessage());
                }
            }

            try {
                String resultadoOS = gerarOrdemServicoParaVenda(vendaId);
                sucessos.add(resultadoOS);
            } catch (Exception e) {
                erros.add("Falha ao gerar ordem de serviço: " + e.getMessage());
            }

        } catch (Exception e) {
            erros.add("Erro geral no processamento: " + e.getMessage());
        }

        boolean success = erros.isEmpty();
        resultado.put("success", success);
        resultado.put("sucessos", sucessos);
        resultado.put("erros", erros);
        resultado.put("message", success ? "Venda processada com sucesso!" : String.join(", ", erros));

        return resultado;
    }

    @Transactional
    public void gerarContasReceberParceladas(Integer vendaId) {
        Venda venda = findById(vendaId);
        if (venda.getDataFechamento() == null) {
            throw new IllegalStateException("Venda deve estar efetivada para gerar contas a receber");
        }

        if (!contaReceberRepository.findByVenda(venda).isEmpty()) {
            throw new IllegalStateException("Contas a receber já foram geradas para esta venda");
        }

        boolean permiteParcelamento = venda.getFormaPagamento().permiteParcelamento();
        Integer numeroParcelas = permiteParcelamento && venda.getNumeroParcelas() != null ? venda.getNumeroParcelas() : 1;
        Integer intervaloDias = 30; // Intervalo padrão
        BigDecimal valorTotal = venda.getTotal().subtract(venda.getDesconto() != null ? venda.getDesconto() : BigDecimal.ZERO);

        List<Parcela> parcelas = gerarParcelas(valorTotal, numeroParcelas, LocalDate.now().plusDays(30), intervaloDias);

        salvarContasReceber(venda, parcelas);
    }

    private void salvarContasReceber(Venda venda, List<Parcela> parcelas) {
        for (Parcela parcela : parcelas) {
            ContaReceber conta = new ContaReceber();
            conta.setVenda(venda);
            conta.setDescricao(String.format("Venda #%d - Parcela %d/%d", venda.getVenId(), parcela.getNumeroParcela(), parcelas.size()));
            conta.setValor(parcela.getValorParcela());
            conta.setDataVencimento(parcela.getDataVencimento());
            conta.setStatus("PENDENTE");
            conta.setDataCriacao(LocalDateTime.now());

            ContaReceber contaSalva = contaReceberRepository.save(conta);

            parcela.setContaReceber(contaSalva);
            parcelaService.salvar(parcela);
        }
    }

    private List<Parcela> gerarParcelas(BigDecimal valorTotal, int numeroParcelas, LocalDate dataVencimentoInicial, int intervaloDias) {
        List<Parcela> parcelas = new ArrayList<>();
        if (numeroParcelas <= 0) numeroParcelas = 1;

        BigDecimal valorParcelaBase = valorTotal.divide(BigDecimal.valueOf(numeroParcelas), 2, RoundingMode.DOWN);
        BigDecimal valorRestante = valorTotal.subtract(valorParcelaBase.multiply(BigDecimal.valueOf(numeroParcelas)));

        for (int i = 1; i <= numeroParcelas; i++) {
            BigDecimal valorDaParcela = valorParcelaBase;
            if (i == numeroParcelas) {
                valorDaParcela = valorDaParcela.add(valorRestante); // Adiciona o resto na última parcela
            }

            LocalDate dataVencimento = dataVencimentoInicial.plusDays((long) (i - 1) * intervaloDias);

            Parcela p = new Parcela();
            p.setNumeroParcela(i);
            p.setTotalParcelas(numeroParcelas);
            p.setValorParcela(valorDaParcela);
            p.setDataVencimento(dataVencimento);
            p.setStatus("PENDENTE");
            parcelas.add(p);
        }
        return parcelas;
    }

    public Venda fromDTO(VendaDTO objDTO) {
        Cliente cliente = clienteRepository.findById(objDTO.getCliente())
                .orElseThrow(() -> new ObjectNotFoundException("Cliente não encontrado! ID: " + objDTO.getCliente()));

        Venda venda = new Venda();
        venda.setVenId(objDTO.getId());
        venda.setDataAbertura(new Date());
        venda.setCliente(cliente);

        VendaTipo tipo = VendaTipo.toEnum(objDTO.getVendaTipo());
        venda.setVendaTipo(tipo);

        venda.setFormaPagamento(FormaPagamento.toEnum(objDTO.getFormaPagamento()));
        venda.setNumeroParcelas(objDTO.getNumeroParcelas() != null ? objDTO.getNumeroParcelas() : 1);
        venda.setDesconto(objDTO.getDesconto());
        venda.setObservacoes(objDTO.getObservacoes());

        if (tipo == VendaTipo.ORCAMENTO) {
            // Se a venda é de um projeto, busca o projeto para obter o valor total
            if (objDTO.getProjetoId() == null) {
                throw new DataIntegrityViolationException("Venda de projeto precisa de um ID de projeto.");
            }
            Projeto projeto = projetoRepository.findById(objDTO.getProjetoId())
                    .orElseThrow(() -> new ObjectNotFoundException("Projeto não encontrado! ID: " + objDTO.getProjetoId()));

            venda.setProjetoId(projeto.getId());
            venda.setTotal(projeto.getValorTotal());
            venda.setItensVenda(new ArrayList<>()); // Garante que não haverá itens
        } else if (tipo == VendaTipo.VENDA) {
            // Se a venda é de produto, processa os itens
            if (objDTO.getItensVenda() != null) {
                List<ItemVenda> itensVenda = objDTO.getItensVenda().stream().map(itemDTO -> {
                    Produto produto = produtoRepository.findById(itemDTO.getProduto())
                            .orElseThrow(() -> new ObjectNotFoundException("Produto não encontrado! ID: " + itemDTO.getProduto()));
                    return new ItemVenda(null, itemDTO.getQuantidade(), itemDTO.getPreco(), null, produto, venda);
                }).collect(Collectors.toList());
                venda.setItensVenda(itensVenda);
            }
        }
        return venda;
    }

    @Transactional
    public Venda update(VendaDTO objDto) {
        Venda venda = fromDTO(objDto);
        Venda existingVenda = findById(venda.getVenId());
        updateData(existingVenda, venda);
        return vendaRepository.save(existingVenda);
    }

    @Transactional
    public Venda addItem(Integer vendaId, Integer itemId) {
        Venda venda = findById(vendaId);
        ItemVenda item = itemVendaRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item não encontrado! ID: " + itemId));
        if (!venda.getItensVenda().contains(item)) {
            venda.addItem(item);
        }
        return vendaRepository.save(venda);
    }

    @Transactional
    public Venda removeItem(Integer vendaId, Integer itemId) {
        Venda venda = findById(vendaId);
        boolean itemRemoved = venda.getItensVenda().removeIf(iv -> iv.getId().equals(itemId));
        if (!itemRemoved) {
            throw new RuntimeException("Item não encontrado na venda! ID: " + itemId);
        }
        return vendaRepository.save(venda);
    }

    public void efetivarVenda(Integer vendaId) {
        Venda venda = findById(vendaId);
        venda.efetuarVenda();
        vendaRepository.save(venda);
    }

    @Transactional
    public String gerarOrdemServicoParaVenda(Integer vendaId) {
        Venda venda = findById(vendaId);
        if (venda.getDataFechamento() == null) {
            throw new IllegalStateException("Venda deve estar efetivada para gerar ordem de serviço");
        }
        if (venda.isVendaProjeto()) {
            return gerarOrdemServicoParaVendaProjeto(venda.getVenId());
        } else if (venda.isVendaProduto()) {
            return "Venda de produto não gera Ordem de Serviço automaticamente.";
        } else {
            throw new IllegalStateException("Tipo de venda não suportado para geração de ordem de serviço");
        }
    }

    private void updateData(Venda existingVenda, Venda venda) {
        existingVenda.setDataAbertura(venda.getDataAbertura());
        existingVenda.setDataFechamento(venda.getDataFechamento());
        existingVenda.setTotal(venda.getTotal());
        existingVenda.setDesconto(venda.getDesconto());
        existingVenda.setVendaTipo(venda.getVendaTipo());
        existingVenda.setFormaPagamento(venda.getFormaPagamento());
        existingVenda.setCliente(venda.getCliente());
        existingVenda.getItensVenda().clear();
        for (ItemVenda item : venda.getItensVenda()) {
            item.setVenda(existingVenda);
            existingVenda.getItensVenda().add(item);
        }
    }


    @Transactional
    public VendaProjetoDTO criarVendaProjeto(VendaProjetoCreateDTO createDTO) {
        Cliente cliente = clienteRepository.findById(createDTO.getClienteId()).orElseThrow(() -> new ObjectNotFoundException("Cliente não encontrado! ID: " + createDTO.getClienteId()));
        Projeto projeto = projetoRepository.findByIdWithDetails(createDTO.getProjetoId()).orElseThrow(() -> new ObjectNotFoundException("Projeto não encontrado! ID: " + createDTO.getProjetoId()));
        if (projeto.getStatus() != StatusProjeto.ORCAMENTO && projeto.getStatus() != StatusProjeto.APROVADO) {
            throw new IllegalStateException("Projeto deve estar em status 'ORÇAMENTO' ou 'APROVADO' para ser vendido");
        }
        if (vendaRepository.findByProjetoId(createDTO.getProjetoId()).isPresent()) {
            throw new IllegalStateException("Já existe uma venda para este projeto");
        }
        Venda venda = new Venda();
        venda.setCliente(cliente);
        venda.setProjetoId(projeto.getId());
        venda.setDataAbertura(new Date());
        venda.setVendaTipo(VendaTipo.ORCAMENTO);
        venda.setFormaPagamento(FormaPagamento.valueOf(createDTO.getFormaPagamento()));
        venda.setNumeroParcelas(createDTO.getNumeroParcelas() != null ? createDTO.getNumeroParcelas() : 1);
        venda.setObservacoes(createDTO.getObservacoes());
        venda.setTotal(projeto.getValorTotal());
        venda.setDesconto(createDTO.getDesconto() != null ? createDTO.getDesconto() : BigDecimal.ZERO);
        venda = vendaRepository.save(venda);
        projeto.setStatus(StatusProjeto.VENDIDO);
        projetoRepository.save(projeto);

        // Reservar materiais automaticamente após criar a venda do projeto
        try {
            estoqueService.reservarMaterialParaVenda(venda.getVenId(), projeto.getId());
        } catch (Exception e) {
            // Log do erro, mas não falha a venda
            System.err.println("Erro ao reservar materiais para venda " + venda.getVenId() + ": " + e.getMessage());
        }

        return convertVendaToProjetoDTO(venda, projeto);
    }

    @Transactional(readOnly = true)
    public VendaProjetoDTO buscarVendaProjetoPorId(Integer id) {
        Venda venda = findById(id);
        if (!venda.isVendaProjeto()) {
            throw new IllegalArgumentException("Esta venda não é uma venda de projeto");
        }
        Projeto projeto = projetoRepository.findByIdWithDetails(venda.getProjetoId())
                .orElseThrow(() -> new ObjectNotFoundException("Projeto não encontrado! ID: " + venda.getProjetoId()));
        return convertVendaToProjetoDTO(venda, projeto);
    }

    @Transactional(readOnly = true)
    public List<VendaProjetoDTO> listarVendasProjetos() {
        return vendaRepository.findByVendaTipoAndProjetoIdIsNotNull(VendaTipo.ORCAMENTO).stream()
                .map(venda -> {
                    Projeto projeto = projetoRepository.findByIdWithDetails(venda.getProjetoId()).orElse(null);
                    return convertVendaToProjetoDTO(venda, projeto);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VendaProjetoDTO> buscarVendasProjetosPorCliente(Integer clienteId) {
        return vendaRepository.findByClienteIdAndVendaTipoAndProjetoIdIsNotNull(clienteId, VendaTipo.ORCAMENTO).stream()
                .map(venda -> {
                    Projeto projeto = projetoRepository.findByIdWithDetails(venda.getProjetoId()).orElse(null);
                    return convertVendaToProjetoDTO(venda, projeto);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VendaProjetoDTO> buscarVendasProjetosPorStatus(String status) {
        List<Venda> vendas;
        if ("ORCAMENTO".equals(status)) {
            vendas = vendaRepository.findByVendaTipoAndDataFechamentoIsNullAndProjetoIdIsNotNull(VendaTipo.ORCAMENTO);
        } else if ("VENDIDO".equals(status)) {
            vendas = vendaRepository.findByVendaTipoAndDataFechamentoIsNotNullAndProjetoIdIsNotNull(VendaTipo.ORCAMENTO);
        } else {
            vendas = vendaRepository.findByVendaTipoAndProjetoIdIsNotNull(VendaTipo.ORCAMENTO);
        }
        return vendas.stream()
                .map(venda -> {
                    Projeto projeto = projetoRepository.findByIdWithDetails(venda.getProjetoId()).orElse(null);
                    return convertVendaToProjetoDTO(venda, projeto);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Transactional
    public VendaProjetoDTO atualizarVendaProjeto(Integer id, VendaProjetoUpdateDTO updateDTO) {
        Venda venda = findById(id);
        if (!venda.isVendaProjeto()) {
            throw new IllegalArgumentException("Esta venda não é uma venda de projeto");
        }
        if (updateDTO.getDesconto() != null) {
            venda.setDesconto(updateDTO.getDesconto());
        }
        if (updateDTO.getFormaPagamento() != null) {
            venda.setFormaPagamento(FormaPagamento.valueOf(updateDTO.getFormaPagamento()));
        }
        if (updateDTO.getNumeroParcelas() != null) {
            venda.setNumeroParcelas(updateDTO.getNumeroParcelas());
        }
        if (updateDTO.getObservacoes() != null) {
            venda.setObservacoes(updateDTO.getObservacoes());
        }
        venda = vendaRepository.save(venda);
        Venda finalVenda = venda;
        Projeto projeto = projetoRepository.findByIdWithDetails(venda.getProjetoId())
                .orElseThrow(() -> new ObjectNotFoundException("Projeto não encontrado! ID: " + finalVenda.getProjetoId()));
        return convertVendaToProjetoDTO(venda, projeto);
    }

    private VendaProjetoDTO convertVendaToProjetoDTO(Venda venda, Projeto projeto) {
        VendaProjetoDTO dto = new VendaProjetoDTO();
        dto.setId(venda.getVenId());
        dto.setClienteId(venda.getCliente().getCliId());
        dto.setDataVenda(venda.getDataAbertura().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());

        if (venda.getDataFechamento() != null) {
            dto.setDataEfetivacao(venda.getDataFechamento().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        }

        dto.setValorTotal(venda.getTotal());
        dto.setDesconto(venda.getDesconto());
        dto.setValorFinal(venda.getTotal().subtract(venda.getDesconto()));
        dto.setFormaPagamento(venda.getFormaPagamento().name());
        dto.setNumeroParcelas(venda.getNumeroParcelas());
        dto.setObservacoes(venda.getObservacoes());
        dto.setStatus(venda.getDataFechamento() != null ? "VENDIDO" : "ORCAMENTO");
        dto.setNomeCliente(venda.getCliente().getPessoa().getNome());

        if (projeto != null) {
            dto.setProjetoId(projeto.getId());
            dto.setNomeProjeto(projeto.getNome());
            dto.setTipoProjeto(projeto.getTipoProjeto().getDescricao());
            dto.setDataPrevistaConclusao(projeto.getDataPrevista());
            dto.setPodeGerarOS(true);
            dto.setPodeGerarContaReceber(true);

            List<ProjetoItem> itens = new ArrayList<>();
            if (projeto.getItens() != null) {
                for (ProjetoItem item : projeto.getItens()) {
                    ProjetoItem itemCopia = new ProjetoItem();
                    itemCopia.setId(item.getId());
                    itemCopia.setQuantidade(item.getQuantidade());
                    itemCopia.setValorUnitario(item.getValorUnitario());

                    if (item.getProduto() != null) {
                        Produto produtoCopia = new Produto();
                        produtoCopia.setProdId(item.getProduto().getProdId());
                        produtoCopia.setNome(item.getProduto().getNome());
                        produtoCopia.setNome(item.getProduto().getNome());
                        itemCopia.setProduto(produtoCopia);
                    }
                    itens.add(itemCopia);
                }
            }

            Projeto projetoLeve = new Projeto();
            projetoLeve.setId(projeto.getId());
            projetoLeve.setNome(projeto.getNome());
            projetoLeve.setDescricao(projeto.getDescricao());
            projetoLeve.setDataPrevista(projeto.getDataPrevista());
            projetoLeve.setItens(itens);
            dto.setProjeto(projetoLeve);
        }

        Cliente clienteLeve = new Cliente();
        clienteLeve.setCliId(venda.getCliente().getCliId());
        clienteLeve.setPessoa(venda.getCliente().getPessoa());
        dto.setCliente(clienteLeve);
        dto.setNomeCliente(venda.getCliente().getPessoa().getNome());

        return dto;
    }
    @Transactional
    public VendaProjetoDTO efetuarVendaProjeto(Integer id) {
        Venda venda = findById(id);

        if (!venda.isVendaProjeto()) {
            throw new IllegalArgumentException("Esta venda não é uma venda de projeto.");
        }
        if (venda.getDataFechamento() != null) {
            throw new IllegalStateException("Esta venda já foi efetivada anteriormente.");
        }

        Venda finalVenda = venda;
        Projeto projeto = projetoRepository.findById(venda.getProjetoId())
                .orElseThrow(() -> new ObjectNotFoundException("Projeto não encontrado! ID: " + finalVenda.getProjetoId()));

        if (venda.getCliente() != null) {
            venda.getCliente().getCliId(); // força inicialização
            venda.getCliente().getPessoa().getNome();
            if (venda.getCliente().getPessoa() != null) {
                venda.getCliente().getPessoa().getId();
                venda.getCliente().getPessoa().getNome();
            }

        }

        if (projeto.getCliente() != null) {
            projeto.getCliente().getCliId();
            projeto.getCliente().getPessoa().getNome();
            if (projeto.getCliente().getPessoa() != null) {
                projeto.getCliente().getPessoa().getId();
                projeto.getCliente().getPessoa().getNome();
            }
        }

        if (projeto.getTipoProjeto() != null) {
            projeto.getTipoProjeto();
            projeto.getTipoProjeto().getDescricao();
        }

        // 4. Itens do projeto
        if (projeto.getItens() != null) {
            for (ProjetoItem item : projeto.getItens()) {
                item.getId();
                item.getQuantidade();
                item.getValorUnitario();
                if (item.getProduto() != null) {
                    item.getProduto().getProdId();
                    item.getProduto().getNome();
                    item.getProduto().getNome();
                }
            }
        }


        venda.setDataFechamento(new Date());
        venda = vendaRepository.save(venda);

        projeto.setStatus(StatusProjeto.VENDIDO);
        projeto = projetoRepository.save(projeto);

        try {
            gerarContasReceberParceladas(venda.getVenId());
        } catch (IllegalStateException e) {
            if (!e.getMessage().contains("já foram geradas")) {
                throw e;
            }
        }

        return convertVendaToProjetoDTO(venda, projeto);
    }

    @Transactional
    public String gerarOrdemServicoParaVendaProjeto(Integer vendaId) {
        Venda venda = findById(vendaId);
        if (venda.getDataFechamento() == null) {
            throw new IllegalStateException("A Venda deve estar efetivada para gerar uma Ordem de Serviço.");
        }
        if (!venda.isVendaProjeto() || venda.getProjetoId() == null) {
            throw new IllegalStateException("Esta venda não é de um projeto ou não tem um projeto associado.");
        }
        if (ordemServicoService.existeOrdemServicoParaProjeto(venda.getProjetoId())) {
            throw new IllegalStateException("Já existe uma Ordem de Serviço para este projeto.");
        }

        OrdemServicoDTO os = ordemServicoService.gerarPorProjeto(venda.getProjetoId());
        return "Ordem de Serviço " + os.getNumero() + " gerada com sucesso!";
    }

    @Transactional
    public String gerarContaReceberParaVendaProjeto(Integer vendaId) {
        VendaProjetoDTO vendaProjeto = buscarVendaProjetoPorId(vendaId);
        if (!"VENDIDO".equals(vendaProjeto.getStatus())) {
            throw new IllegalStateException("Venda deve estar efetivada para gerar conta a receber");
        }
        if (vendaProjeto.getContaReceberGerada()) {
            throw new IllegalStateException("Conta a receber já foi gerada para esta venda");
        }
        try {
            ContaReceberDTO contaReceber = new ContaReceberDTO();
            contaReceber.setProjetoId(vendaProjeto.getProjetoId());
            contaReceber.setValor(vendaProjeto.getValorFinal());
            contaReceber.setDataVencimento(vendaProjeto.getDataPrevistaConclusao());
            contaReceber.setStatus("PENDENTE");
            contaReceber.setObservacoes("Conta gerada automaticamente da venda de projeto");

            return "Conta a receber de projeto gerada com sucesso"; // Placeholder
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar conta a receber: " + e.getMessage());
        }
    }
}
