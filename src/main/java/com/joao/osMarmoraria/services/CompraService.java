package com.joao.osMarmoraria.services;

import com.joao.osMarmoraria.domain.*;
import com.joao.osMarmoraria.domain.enums.FormaPagamento;
import com.joao.osMarmoraria.dtos.CompraDTO;
import com.joao.osMarmoraria.dtos.ContaPagarDTO;
import com.joao.osMarmoraria.dtos.InstallmentRequestDTO;
import com.joao.osMarmoraria.dtos.ItemCompraDTO;
import com.joao.osMarmoraria.repository.*;
import com.joao.osMarmoraria.services.exceptions.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class CompraService {

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private ItemCompraRepository itemRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private FornecedorRepository fornecedorRepository;

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    @Autowired
    private ContaPagarService contaPagarService;
    
    @Autowired
    private ParcelaService parcelaService;

    @Transactional(readOnly = true)
    public Compra findById(Integer id) {
        return compraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compra não encontrada! ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<Compra> findAll() {
        return compraRepository.findAll();
    }

    public BigDecimal calculateTotals(List<ItemCompra> itens) {
        BigDecimal totalValue = BigDecimal.ZERO;
        BigDecimal totalQuantity = BigDecimal.ZERO;

        for (ItemCompra ic : itens) {
            // Verifique se o item está presente no repositório
            ItemCompra item = itemRepository.findById(ic.getId())
                    .orElseThrow(() -> new RuntimeException("Item não encontrado! ID: " + ic.getId()));
            // Calcule o total do item
            BigDecimal itemTotal = item.getValor().multiply(item.getQuantidade());
            totalValue = totalValue.add(itemTotal);
            totalQuantity = totalQuantity.add(item.getQuantidade());
        }

        return totalValue;
    }

    @Transactional
    public Compra create(CompraDTO objDto) {
        Compra compra = fromDTO(objDto);
        List<ItemCompra> itensTemporarios = new ArrayList<>(compra.getItensCompra());
        compra.getItensCompra().clear();
        BigDecimal valorTotalCalculado = BigDecimal.ZERO;
        BigDecimal quantidadeTotalCalculada = BigDecimal.ZERO;

        for (ItemCompra item : itensTemporarios) {
            item.setCompra(compra);
            compra.getItensCompra().add(item);
            Produto produto = produtoRepository.findById(item.getProduto().getProdId())
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado! ID: " + item.getProduto().getProdId()));
            produto.aumentarEstoque(item.getQuantidade());
            produtoRepository.save(produto);
            valorTotalCalculado = valorTotalCalculado.add(item.getValor().multiply(item.getQuantidade()));
            quantidadeTotalCalculada = quantidadeTotalCalculada.add(item.getQuantidade());
        }

        compra.setValorTotal(valorTotalCalculado);
        compra.setQuantidadeTotal(quantidadeTotalCalculada);

        ContaPagar contaPagar = new ContaPagar();
        contaPagar.setCompra(compra);
        contaPagar.setValor(compra.getValorTotal());
        contaPagar.setDataVencimento(LocalDate.now().plusDays(30));
        contaPagar.setStatus("PENDENTE");
        
        if (compra.isParcelado()) {
            contaPagar.setParcelado(true);
            contaPagar.setNumeroParcelas(compra.getNumeroParcelas());
        }
        
        compra.getContasPagar().add(contaPagar);

        compra = compraRepository.save(compra);
        
        contaPagarService.criar(new ContaPagarDTO(contaPagar));
        
        if (compra.isParcelado()) {
            InstallmentRequestDTO installmentRequest = new InstallmentRequestDTO(
                compra.getNumeroParcelas(),
                compra.getIntervaloParcelas(),
                compra.getValorTotal()
            );
            parcelaService.gerarParcelasParaCompra(compra, installmentRequest);
        }

        return compra;
    }

    @Transactional
    public Compra update(CompraDTO objDto) {
        Compra compra = fromDTO(objDto);
        Compra existingCompra = findById(compra.getComprId());
        for (ItemCompra item : compra.getItensCompra()) {
            Optional<Produto> produto = produtoRepository.findById(item.getProduto().getProdId());
            produto.orElseThrow(() -> new RuntimeException("Produto não Encontrado ID: "
                    + item.getProduto().getProdId())).aumentarEstoque(item.getQuantidade());
        }
        updateData(existingCompra, compra);
        return compraRepository.save(existingCompra);
    }

    @Transactional
    public Compra addItem(Integer compraId, Integer itemId) {
        Compra compra = findById(compraId);
        ItemCompra item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item não encontrado! ID: " + itemId));

        if (!compra.getItensCompra().contains(item)) {
            compra.getItensCompra().add(item);
            item.setCompra(compra);
        }

        return compraRepository.save(compra);
    }

    @Transactional
    public Compra removeItem(Integer compraId, Integer itemId) {
        Compra compra = findById(compraId);
        ItemCompra itemToRemove = null;
        for (ItemCompra ic : compra.getItensCompra()) {
            if (ic.getId().equals(itemId)) {
                itemToRemove = ic;
                break;
            }
        }

        if (itemToRemove == null) {
            throw new RuntimeException("Item não encontrado na compra! ID: " + itemId);
        }

        compra.getItensCompra().remove(itemToRemove);
        itemToRemove.setCompra(null);

        return compraRepository.save(compra);
    }

    private void updateData(Compra existingCompra, Compra compra) {
        existingCompra.setObservacoes(compra.getObservacoes());
        existingCompra.setValorTotal(compra.getValorTotal());
        existingCompra.setQuantidadeTotal(compra.getQuantidadeTotal());
        existingCompra.setDataCompra(compra.getDataCompra());
        existingCompra.setFornecedor(compra.getFornecedor());
        existingCompra.setFuncionario(compra.getFuncionario());
        existingCompra.setFormaPagamento(compra.getFormaPagamento());

        existingCompra.getItensCompra().clear();

        for (ItemCompra item : compra.getItensCompra()) {
            ItemCompra managedItem = item.getId() != null ?
                    itemRepository.findById(item.getId())
                            .orElseThrow(() -> new RuntimeException("Item não encontrado! ID: " + item.getId())) :
                    item;
            managedItem.setCompra(existingCompra);
            existingCompra.getItensCompra().add(managedItem);
        }
    }

    public Compra fromDTO(CompraDTO objDto) {
        Optional<Fornecedor> fornecedor = fornecedorRepository.findById(objDto.getFornecedor());
        Optional<Funcionario> funcionario = funcionarioRepository.findById(objDto.getFuncionario());


        List<ItemCompra> items = new ArrayList<>();
        for (ItemCompraDTO itemDto : objDto.getItensCompra()) {
            Produto produto = produtoRepository.findById(itemDto.getProduto())
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado! ID: " + itemDto.getProduto()));

            ItemCompra item = new ItemCompra();
            item.setId(itemDto.getId());
            item.setQuantidade(itemDto.getQuantidade());
            item.setValor(itemDto.getValor());
            item.setProduto(produto);
            items.add(item);
        }

        return new Compra(
                objDto.getComprId(),
                objDto.getObservacoes(),
                objDto.getDataCompra() != null ? objDto.getDataCompra() : new Date(),
                fornecedor.get(),
                funcionario.get(),
                FormaPagamento.toEnum(objDto.getFormaPagamento()),
                items
        );
    }
}