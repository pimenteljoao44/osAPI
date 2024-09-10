package com.joao.osMarmoraria.services;

import com.joao.osMarmoraria.domain.*;
import com.joao.osMarmoraria.domain.enums.FormaPagamento;
import com.joao.osMarmoraria.dtos.CompraDTO;
import com.joao.osMarmoraria.dtos.ItemCompraDTO;
import com.joao.osMarmoraria.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

        // Salve a Compra primeiro
        Compra savedCompra = compraRepository.save(compra);

        // Atribua a Compra salva aos itens
        for (ItemCompra item : savedCompra.getItensCompra()) {
            item.setCompra(savedCompra);
            itemRepository.save(item);
        }

        // Atualize as quantidades em estoque
        for (ItemCompra item : savedCompra.getItensCompra()) {
            Produto produto = produtoRepository.findById(item.getProduto().getProdId())
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado! ID: " + item.getProduto().getProdId()));
            produto.aumentarEstoque(item.getQuantidade());
            produtoRepository.save(produto);
        }

        // Calcule e atualize os totais
        BigDecimal totalValue = calculateTotals(savedCompra.getItensCompra());
        savedCompra.setValorTotal(totalValue);
        savedCompra.setQuantidadeTotal(BigDecimal.valueOf(savedCompra.getItensCompra().size()));

        return compraRepository.save(savedCompra);
    }

    @Transactional
    public Compra update(CompraDTO objDto) {
        Compra compra = fromDTO(objDto);
        Compra existingCompra = findById(compra.getComprId());
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

        // Remove itens antigos
        existingCompra.getItensCompra().clear();

        // Adiciona novos itens, garantindo que são entidades gerenciadas
        for (ItemCompra item : compra.getItensCompra()) {
            ItemCompra managedItem = item.getId() != null ?
                    itemRepository.findById(item.getId())
                            .orElseThrow(() -> new RuntimeException("Item não encontrado! ID: " + item.getId())) :
                    item; // Se o item é novo, não precisa buscar no repositório
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
            // Inicialize sem definir a referência da Compra
            item.setCompra(null);
            items.add(item);
        }

        return new Compra(
                objDto.getComprId(),
                objDto.getObservacoes(),
                objDto.getValorTotal(),
                objDto.getQuantidadeTotal(),
                objDto.getDataCompra(),
                fornecedor.orElseThrow(() -> new RuntimeException("Fornecedor não encontrado! ID: " + objDto.getFornecedor())),
                funcionario.orElseThrow(() -> new RuntimeException("Funcionário não encontrado! ID: " + objDto.getFuncionario())),
                FormaPagamento.toEnum(objDto.getFormaPagamento()),
                items
        );
    }
}
