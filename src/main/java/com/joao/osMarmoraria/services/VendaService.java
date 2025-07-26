package com.joao.osMarmoraria.services;

import com.joao.osMarmoraria.domain.*;
import com.joao.osMarmoraria.domain.enums.FormaPagamento;
import com.joao.osMarmoraria.domain.enums.StatusProjeto;
import com.joao.osMarmoraria.domain.enums.VendaTipo;
import com.joao.osMarmoraria.dtos.*;
import com.joao.osMarmoraria.repository.*;
import com.joao.osMarmoraria.services.exceptions.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
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
    private ContaReceberService contaReceberService;

    @Autowired
    private OrdemServicoService ordemServicoService;

    @Autowired
    private ParcelaService parcelaService;

    @Transactional(readOnly = true)
    public Venda findById(Integer id) {
        return vendaRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Venda não encontrada! ID: " + id));
    }

    public List<Venda> findAll() {
        return vendaRepository.findAll();
    }

    public Venda create(VendaDTO objDto) {
        Venda venda = fromDTO(objDto);

        Venda savedVenda = vendaRepository.save(venda);

        List<ItemVenda> itensVendaCopia = new ArrayList<>(savedVenda.getItensVenda());

        for (ItemVenda item : itensVendaCopia) {
            item.setVenda(savedVenda);
            itemVendaRepository.save(item);
        }

        for (ItemVenda item : itensVendaCopia) {
            Produto produto = produtoRepository.findById(item.getProduto().getProdId())
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado! ID: " + item.getProduto().getProdId()));
            produto.baixarEstoque(item.getQuantidade());
            produtoRepository.save(produto);
        }

        BigDecimal valorTotal = venda.calculaTotal();
        savedVenda.setTotal(valorTotal);
        return vendaRepository.save(savedVenda);
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
            venda.addItem(item); // Atualiza o item e recalcula o total
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

    @Transactional
    public Venda efetuarVenda(Integer vendaId) {
        Venda venda = findById(vendaId);
        venda.efetuarVenda(); // Atualiza a data de fechamento e recalcula o total

        return vendaRepository.save(venda);
    }

    // ========== MÉTODOS PARA VENDA-PROJETO ==========

    @Transactional
    public VendaProjetoDTO criarVendaProjeto(VendaProjetoCreateDTO createDTO) {
        // Verificar se cliente existe
        Cliente cliente = clienteRepository.findById(createDTO.getClienteId())
                .orElseThrow(() -> new ObjectNotFoundException("Cliente não encontrado! ID: " + createDTO.getClienteId()));

        // Verificar se projeto existe
        Projeto projeto = projetoRepository.findByIdWithDetails(createDTO.getProjetoId())
                .orElseThrow(() -> new ObjectNotFoundException("Projeto não encontrado! ID: " + createDTO.getProjetoId()));

        // Verificar se projeto pode ser vendido
        if (projeto.getStatus() != StatusProjeto.ORCAMENTO && projeto.getStatus() != StatusProjeto.APROVADO) {
            throw new IllegalStateException("Projeto deve estar em status 'ORÇAMENTO' ou 'APROVADO' para ser vendido");
        }

        // Verificar se já existe venda para este projeto
        Optional<Venda> vendaExistente = vendaRepository.findByProjetoId(createDTO.getProjetoId());
        if (vendaExistente.isPresent()) {
            throw new IllegalStateException("Já existe uma venda para este projeto");
        }

        // Criar venda de projeto
        Venda venda = new Venda();
        venda.setCliente(cliente);
        venda.setProjetoId(projeto.getId());
        venda.setDataAbertura(new Date());
        venda.setVendaTipo(VendaTipo.ORCAMENTO);
        venda.setFormaPagamento(FormaPagamento.valueOf(createDTO.getFormaPagamento()));
        venda.setNumeroParcelas(createDTO.getNumeroParcelas() != null ? createDTO.getNumeroParcelas() : 1);
        venda.setObservacoes(createDTO.getObservacoes());

        // Calcular valores
        BigDecimal valorTotal = projeto.getValorTotal();
        BigDecimal desconto = createDTO.getDesconto() != null ? createDTO.getDesconto() : BigDecimal.ZERO;
        BigDecimal valorFinal = valorTotal.subtract(desconto);

        venda.setTotal(valorTotal);
        venda.setDesconto(desconto);

        venda = vendaRepository.save(venda);

        // Atualizar status do projeto para VENDIDO
        projeto.setStatus(StatusProjeto.VENDIDO);
        projetoRepository.save(projeto);

        // Gerar parcelas se necessário
        if (venda.getNumeroParcelas() > 1) {
            gerarParcelasVendaProjeto(venda, projeto);
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
        List<Venda> vendas = vendaRepository.findByVendaTipoAndProjetoIdIsNotNull(VendaTipo.ORCAMENTO);
        return vendas.stream()
                .map(venda -> {
                    try {
                        Projeto projeto = projetoRepository.findByIdWithDetails(venda.getProjetoId())
                                .orElse(null);
                        return convertVendaToProjetoDTO(venda, projeto);
                    } catch (Exception e) {
                        // Log error and continue
                        return null;
                    }
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VendaProjetoDTO> buscarVendasProjetosPorCliente(Integer clienteId) {
        List<Venda> vendas = vendaRepository.findByClienteIdAndVendaTipoAndProjetoIdIsNotNull(clienteId, VendaTipo.ORCAMENTO);
        return vendas.stream()
                .map(venda -> {
                    try {
                        Projeto projeto = projetoRepository.findByIdWithDetails(venda.getProjetoId())
                                .orElse(null);
                        return convertVendaToProjetoDTO(venda, projeto);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(dto -> dto != null)
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
                    try {
                        Projeto projeto = projetoRepository.findByIdWithDetails(venda.getProjetoId())
                                .orElse(null);
                        return convertVendaToProjetoDTO(venda, projeto);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    @Transactional
    public VendaProjetoDTO atualizarVendaProjeto(Integer id, VendaProjetoUpdateDTO updateDTO) {
        Venda venda = findById(id);

        if (!venda.isVendaProjeto()) {
            throw new IllegalArgumentException("Esta venda não é uma venda de projeto");
        }

        // Atualizar campos editáveis
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

    @Transactional
    public VendaProjetoDTO efetuarVendaProjeto(Integer id) {
        Venda venda = findById(id);

        if (!venda.isVendaProjeto()) {
            throw new IllegalArgumentException("Esta venda não é uma venda de projeto");
        }

        if (venda.getDataFechamento() != null) {
            throw new IllegalStateException("Esta venda já foi efetivada");
        }

        venda.setDataFechamento(new Date());
        venda = vendaRepository.save(venda);

        Venda finalVenda = venda;
        Projeto projeto = projetoRepository.findByIdWithDetails(venda.getProjetoId())
                .orElseThrow(() -> new ObjectNotFoundException("Projeto não encontrado! ID: " + finalVenda.getProjetoId()));

        // Atualizar status do projeto
        projeto.setStatus(StatusProjeto.VENDIDO);
        projetoRepository.save(projeto);

        return convertVendaToProjetoDTO(venda, projeto);
    }

    @Transactional
    public String gerarOrdemServicoParaVendaProjeto(Integer vendaId) {
        VendaProjetoDTO vendaProjeto = buscarVendaProjetoPorId(vendaId);

        if (!"VENDIDO".equals(vendaProjeto.getStatus())) {
            throw new IllegalStateException("Venda deve estar efetivada para gerar ordem de serviço");
        }

        if (vendaProjeto.getOrdemServicoGerada()) {
            throw new IllegalStateException("Ordem de serviço já foi gerada para esta venda");
        }

        try {
            OrdemServicoDTO ordemServico = ordemServicoService.gerarPorProjeto(vendaProjeto.getProjetoId());
            return "Ordem de serviço " + ordemServico.getNumero() + " gerada com sucesso!";
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar ordem de serviço: " + e.getMessage());
        }
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

            ContaReceberDTO novaConta = contaReceberService.criarPorProjeto(contaReceber);
            return "Conta a receber gerada com sucesso! ID: " + novaConta.getId();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar conta a receber: " + e.getMessage());
        }
    }

    private void gerarParcelasVendaProjeto(Venda venda, Projeto projeto) {
        try {
            InstallmentRequestDTO installmentRequest = new InstallmentRequestDTO();
            installmentRequest.setValorTotal(venda.getTotal().subtract(venda.getDesconto()));
            installmentRequest.setNumeroParcelas(venda.getNumeroParcelas());
            installmentRequest.setDataPrimeiroVencimento(LocalDate.now().plusDays(30));
            installmentRequest.setObservacoes("Parcelas geradas automaticamente da venda do projeto: " + projeto.getNome());

            parcelaService.gerarParcelasParaVenda(venda,installmentRequest);
        } catch (Exception e) {
            // Log error but don't fail the transaction
            System.err.println("Erro ao gerar parcelas para venda de projeto: " + e.getMessage());
        }
    }

    // ========== MÉTODOS AUXILIARES ==========

    private void updateData(Venda existingVenda, Venda venda) {
        existingVenda.setDataAbertura(venda.getDataAbertura());
        existingVenda.setDataFechamento(venda.getDataFechamento());
        existingVenda.setTotal(venda.getTotal());
        existingVenda.setDesconto(venda.getDesconto());
        existingVenda.setVendaTipo(venda.getVendaTipo());
        existingVenda.setFormaPagamento(venda.getFormaPagamento());
        existingVenda.setCliente(venda.getCliente());

        List<ItemVenda> itensVendaCopia = new ArrayList<>(venda.getItensVenda());

        existingVenda.getItensVenda().clear();

        for (ItemVenda item : itensVendaCopia) {
            ItemVenda managedItem = item.getId() != null ?
                    itemVendaRepository.findById(item.getId())
                            .orElseThrow(() -> new RuntimeException("Item não encontrado! ID: " + item.getId())) :
                    item;
            managedItem.setVenda(existingVenda);
            existingVenda.getItensVenda().add(managedItem);
        }
    }

    public Venda fromDTO(VendaDTO objDTO) {
        Cliente cliente = clienteRepository.findById(objDTO.getCliente())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado! ID: " + objDTO.getCliente()));

        List<ItemVenda> itensVenda = new ArrayList<>();
        for (ItemVendaDTO itemDTO : objDTO.getItensVenda()) {
            Produto produto = produtoRepository.findById(itemDTO.getProduto())
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado! ID: " + itemDTO.getProduto()));

            ItemVenda itemVenda = new ItemVenda();
            itemVenda.setId(itemDTO.getId());
            itemVenda.setQuantidade(itemDTO.getQuantidade());
            itemVenda.setPreco(itemDTO.getPreco());
            itemVenda.setProduto(produto);
            itemVenda.setVenda(null);

            itensVenda.add(itemVenda);
        }

        return new Venda(
                objDTO.getId(),
                objDTO.getDataAbertura(),
                objDTO.getDataFechamento(),
                objDTO.getTotal(),
                objDTO.getDesconto(),
                VendaTipo.toEnum(objDTO.getVendaTipo()),
                FormaPagamento.toEnum(objDTO.getFormaPagamento()),
                itensVenda,
                cliente
        );
    }

    private VendaProjetoDTO convertVendaToProjetoDTO(Venda venda, Projeto projeto) {
        VendaProjetoDTO dto = new VendaProjetoDTO();
        dto.setId(venda.getVenId());
        dto.setClienteId(venda.getCliente().getCliId());
        dto.setCliente(venda.getCliente());
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

        // Informações do cliente
        dto.setNomeCliente(venda.getCliente().getPessoa().getNome());

        // Informações do projeto
        if (projeto != null) {
            dto.setProjetoId(projeto.getId());
            dto.setProjeto(projeto);
            dto.setNomeProjeto(projeto.getNome());
            dto.setTipoProjeto(projeto.getTipoProjeto().getDescricao());
            dto.setDataPrevistaConclusao(projeto.getDataPrevista());

            // Verificar se pode gerar OS e conta a receber
            dto.setPodeGerarOS("VENDIDO".equals(dto.getStatus()) && !dto.getOrdemServicoGerada());
            dto.setPodeGerarContaReceber("VENDIDO".equals(dto.getStatus()) && !dto.getContaReceberGerada());
        }

        return dto;
    }
}