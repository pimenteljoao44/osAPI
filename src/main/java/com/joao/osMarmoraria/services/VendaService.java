package com.joao.osMarmoraria.services;

import com.joao.osMarmoraria.domain.*;
import com.joao.osMarmoraria.domain.enums.FormaPagamento;
import com.joao.osMarmoraria.domain.enums.StatusProjeto;
import com.joao.osMarmoraria.domain.enums.VendaTipo;
import com.joao.osMarmoraria.dtos.*;
import com.joao.osMarmoraria.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    @Transactional(readOnly = true)
    public Venda findById(Integer id) {
        return vendaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venda não encontrada! ID: " + id));
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

    @Transactional
    public VendaProjetoDTO criarVendaProjeto(VendaProjetoDTO vendaProjetoDTO) {
        // Verificar se cliente existe
        Cliente cliente = clienteRepository.findById(vendaProjetoDTO.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado! ID: " + vendaProjetoDTO.getClienteId()));

        // Verificar se projeto existe
        Projeto projeto = projetoRepository.findById(vendaProjetoDTO.getProjetoId())
                .orElseThrow(() -> new RuntimeException("Projeto não encontrado! ID: " + vendaProjetoDTO.getProjetoId()));

        // Verificar se projeto pode ser vendido
        if (!"ORCAMENTO".equals(projeto.getStatus().name()) && !"APROVADO".equals(projeto.getStatus().name())) {
            throw new RuntimeException("Projeto deve estar em status 'ORÇAMENTO' ou 'APROVADO' para ser vendido");
        }

        // Criar venda de projeto (usando a entidade Venda existente com adaptações)
        Venda venda = new Venda();
        venda.setCliente(cliente);
        venda.setDataAbertura(new Date());
        venda.setTotal(vendaProjetoDTO.getValorTotal());
        venda.setDesconto(vendaProjetoDTO.getDesconto() != null ? vendaProjetoDTO.getDesconto() : BigDecimal.ZERO);
        venda.setVendaTipo(VendaTipo.ORCAMENTO); // Assumindo que existe este enum
        venda.setFormaPagamento(FormaPagamento.valueOf(vendaProjetoDTO.getFormaPagamento()));

        // Calcular valor final
        BigDecimal valorFinal = venda.getTotal().subtract(venda.getDesconto());
        venda.setTotal(valorFinal);

        venda = vendaRepository.save(venda);

        // Atualizar status do projeto para VENDIDO
        projeto.setStatus(StatusProjeto.VENDIDO);
        projetoRepository.save(projeto);

        return convertVendaToProjetoDTO(venda, projeto);
    }

    @Transactional(readOnly = true)
    public VendaProjetoDTO buscarVendaProjetoPorId(Integer id) {
        Venda venda = findById(id);

        // Buscar projeto relacionado (assumindo que existe uma forma de relacionar)
        // Por enquanto, vou buscar pelo cliente e status
        List<Projeto> projetos = projetoRepository.findByClienteIdAndStatus(venda.getCliente().getCliId(), StatusProjeto.VENDIDO);
        Projeto projeto = projetos.isEmpty() ? null : projetos.get(0);

        return convertVendaToProjetoDTO(venda, projeto);
    }

    @Transactional(readOnly = true)
    public List<VendaProjetoDTO> listarVendasProjetos() {
        List<Venda> vendas = vendaRepository.findByVendaTipo(VendaTipo.ORCAMENTO);
        return vendas.stream()
                .map(venda -> {
                    List<Projeto> projetos = projetoRepository.findByClienteIdAndStatus(venda.getCliente().getCliId(), StatusProjeto.VENDIDO);
                    Projeto projeto = projetos.isEmpty() ? null : projetos.get(0);
                    return convertVendaToProjetoDTO(venda, projeto);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public VendaProjetoDTO atualizarVendaProjeto(VendaProjetoDTO vendaProjetoDTO) {
        Venda venda = findById(vendaProjetoDTO.getId());

        // Atualizar campos editáveis
        venda.setTotal(vendaProjetoDTO.getValorTotal());
        venda.setDesconto(vendaProjetoDTO.getDesconto());
        venda.setFormaPagamento(FormaPagamento.valueOf(vendaProjetoDTO.getFormaPagamento()));

        // Recalcular valor final
        BigDecimal valorFinal = venda.getTotal().subtract(venda.getDesconto());
        venda.setTotal(valorFinal);

        venda = vendaRepository.save(venda);

        // Buscar projeto relacionado
        List<Projeto> projetos = projetoRepository.findByClienteIdAndStatus(venda.getCliente().getCliId(), StatusProjeto.VENDIDO);
        Projeto projeto = projetos.isEmpty() ? null : projetos.get(0);

        return convertVendaToProjetoDTO(venda, projeto);
    }

    @Transactional
    public VendaProjetoDTO efetuarVendaProjeto(Integer id) {
        Venda venda = findById(id);
        venda.setDataFechamento(new Date());
        venda = vendaRepository.save(venda);

        // Buscar projeto relacionado
        List<Projeto> projetos = projetoRepository.findByClienteIdAndStatus(venda.getCliente().getCliId(), StatusProjeto.VENDIDO);
        Projeto projeto = projetos.isEmpty() ? null : projetos.get(0);

        return convertVendaToProjetoDTO(venda, projeto);
    }

    @Transactional(readOnly = true)
    public List<VendaProjetoDTO> buscarVendasProjetosPorCliente(Integer clienteId) {
        List<Venda> vendas = vendaRepository.findByClienteIdAndVendaTipo(clienteId, VendaTipo.ORCAMENTO);
        return vendas.stream()
                .map(venda -> {
                    List<Projeto> projetos = projetoRepository.findByClienteIdAndStatus(clienteId, StatusProjeto.VENDIDO);
                    Projeto projeto = projetos.isEmpty() ? null : projetos.get(0);
                    return convertVendaToProjetoDTO(venda, projeto);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public String gerarOrdemServicoParaVendaProjeto(Integer vendaId) {
        VendaProjetoDTO vendaProjeto = buscarVendaProjetoPorId(vendaId);

        if (vendaProjeto.getProjetoId() == null) {
            throw new RuntimeException("Projeto não encontrado para esta venda");
        }

        if (vendaProjeto.getOrdemServicoGerada()) {
            throw new RuntimeException("Ordem de serviço já foi gerada para esta venda");
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

        if (vendaProjeto.getContaReceberGerada()) {
            throw new RuntimeException("Conta a receber já foi gerada para esta venda");
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