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
import java.math.RoundingMode;
import java.time.LocalDate;
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
                .orElseThrow(() -> new ObjectNotFoundException("Compra não encontrada! ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<Compra> findAll() {
        return compraRepository.findAll();
    }

    @Transactional
    public Compra create(CompraDTO objDto) {
        Compra compra = fromDTO(objDto);
        compra.setValorTotal(compra.calculaTotal());
        compra.setQuantidadeTotal(compra.calculaQuantidadeTotal());

        Compra savedCompra = compraRepository.save(compra);

        savedCompra.getItensCompra().forEach(item -> {
            item.setCompra(savedCompra);
            itemRepository.save(item);

            Produto produto = item.getProduto();
            produto.aumentarEstoque(item.getQuantidade());
            produtoRepository.save(produto);
        });


        gerarContasPagarParceladas(savedCompra.getComprId());

        return savedCompra;
    }

    @Transactional
    public Compra update(CompraDTO objDto) {
        Compra existingCompra = findById(objDto.getComprId());

        existingCompra.getItensCompra().forEach(item -> {
            Produto produto = item.getProduto();
            produto.baixarEstoque(item.getQuantidade());
            produtoRepository.save(produto);
        });

        existingCompra.getItensCompra().clear();
        itemRepository.deleteById(existingCompra.getComprId());


        Compra compraAtualizada = fromDTO(objDto);

        updateData(existingCompra, compraAtualizada);

        compraAtualizada.getItensCompra().forEach(novoItem -> {
            novoItem.setCompra(existingCompra);
            existingCompra.getItensCompra().add(novoItem);

            Produto produto = novoItem.getProduto();
            produto.aumentarEstoque(novoItem.getQuantidade());
            produtoRepository.save(produto);
        });

        existingCompra.setValorTotal(existingCompra.calculaTotal());
        existingCompra.setQuantidadeTotal(existingCompra.calculaQuantidadeTotal());

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

        compra.setValorTotal(compra.calculaTotal());
        compra.setQuantidadeTotal(compra.calculaQuantidadeTotal());

        return compraRepository.save(compra);
    }

    @Transactional
    public Compra removeItem(Integer compraId, Integer itemId) {
        Compra compra = findById(compraId);

        ItemCompra itemToRemove = compra.getItensCompra().stream()
                .filter(ic -> ic.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item não encontrado na compra! ID: " + itemId));

        compra.getItensCompra().remove(itemToRemove);
        itemToRemove.setCompra(null);

        compra.setValorTotal(compra.calculaTotal());
        compra.setQuantidadeTotal(compra.calculaQuantidadeTotal());

        return compraRepository.save(compra);
    }


    private void updateData(Compra existingCompra, Compra compra) {
        existingCompra.setObservacoes(compra.getObservacoes());
        existingCompra.setDataCompra(compra.getDataCompra());
        existingCompra.setFornecedor(compra.getFornecedor());
        existingCompra.setFuncionario(compra.getFuncionario());
        existingCompra.setFormaPagamento(compra.getFormaPagamento());
        existingCompra.setNumeroParcelas(compra.getNumeroParcelas());
        existingCompra.setIntervaloParcelas(compra.getIntervaloParcelas());
    }

    public Compra fromDTO(CompraDTO objDto) {
        Fornecedor fornecedor = fornecedorRepository.findById(objDto.getFornecedor())
                .orElseThrow(() -> new ObjectNotFoundException("Fornecedor não encontrado! ID: " + objDto.getFornecedor()));
        Funcionario funcionario = funcionarioRepository.findById(objDto.getFuncionario())
                .orElseThrow(() -> new ObjectNotFoundException("Funcionário não encontrado! ID: " + objDto.getFuncionario()));

        Compra compra = new Compra();
        compra.setComprId(objDto.getComprId());
        compra.setObservacoes(objDto.getObservacoes());
        compra.setDataCompra(objDto.getDataCompra() != null ? objDto.getDataCompra() : new Date());
        compra.setFornecedor(fornecedor);
        compra.setFuncionario(funcionario);
        compra.setFormaPagamento(FormaPagamento.toEnum(objDto.getFormaPagamento()));

        if (objDto.getInstallmentRequest() != null && objDto.getInstallmentRequest().isValid()) {
            compra.setNumeroParcelas(objDto.getInstallmentRequest().getNumeroParcelas());
            compra.setIntervaloParcelas(objDto.getInstallmentRequest().getIntervaloDias());
        } else {
            compra.setNumeroParcelas(objDto.getNumeroParcelas() != null ? objDto.getNumeroParcelas() : 1);
            compra.setIntervaloParcelas(objDto.getIntervaloParcelas() != null ? objDto.getIntervaloParcelas() : 30);
        }

        if (objDto.getItensCompra() != null) {
            objDto.getItensCompra().forEach(itemDto -> {
                Produto produto = produtoRepository.findById(itemDto.getProduto())
                        .orElseThrow(() -> new ObjectNotFoundException("Produto não encontrado! ID: " + itemDto.getProduto()));

                ItemCompra item = new ItemCompra(null, itemDto.getQuantidade(), itemDto.getValor(), compra, produto);
                compra.getItensCompra().add(item);
            });
        }
        return compra;
    }

    @Transactional
    public void gerarContasPagarParceladas(Integer compraId) {
        Compra compra = findById(compraId);

        if (!contaPagarService.buscarPorCompra(compraId).isEmpty()) {
            throw new IllegalStateException("Contas a pagar já foram geradas para esta compra");
        }

        boolean permiteParcelamento = compra.getFormaPagamento().permiteParcelamento();
        Integer numeroParcelas = permiteParcelamento && compra.getNumeroParcelas() != null ? compra.getNumeroParcelas() : 1;
        Integer intervaloDias = compra.getIntervaloParcelas() != null ? compra.getIntervaloParcelas() : 30;

        List<Parcela> parcelas = gerarParcelas(compra.getValorTotal(), numeroParcelas, LocalDate.now().plusDays(30), intervaloDias);

        salvarContasPagar(compra, parcelas);
    }



    private void salvarContasPagar(Compra compra, List<Parcela> parcelas) {
        for (Parcela parcela : parcelas) {
            ContaPagar conta = new ContaPagar();
            conta.setCompra(compra);
            conta.setDescricao(String.format("Compra #%d - Parcela %d/%d", compra.getComprId(), parcela.getNumeroParcela(), parcelas.size()));
            conta.setValor(parcela.getValorParcela());
            conta.setDataVencimento(parcela.getDataVencimento());
            conta.setStatus("PENDENTE");

            ContaPagarDTO contaDTO = new ContaPagarDTO(conta);
            ContaPagar contaSalva = contaPagarService.criar(contaDTO);

            parcela.setContaPagar(contaSalva);
            parcelaService.salvar(parcela);
        }
    }

    private List<Parcela> gerarParcelas(BigDecimal valorTotal, int numeroParcelas, LocalDate dataVencimentoInicial, int intervaloDias) {
        List<Parcela> parcelas = new ArrayList<>();
        if (numeroParcelas <= 0) numeroParcelas = 1;

        BigDecimal valorParcela = valorTotal.divide(BigDecimal.valueOf(numeroParcelas), 2, RoundingMode.DOWN);
        BigDecimal valorRestante = valorTotal.subtract(valorParcela.multiply(BigDecimal.valueOf(numeroParcelas)));

        for (int i = 1; i <= numeroParcelas; i++) {
            BigDecimal valorFinalParcela = valorParcela;
            if (i == numeroParcelas) {
                valorFinalParcela = valorFinalParcela.add(valorRestante);
            }

            LocalDate dataVencimento = dataVencimentoInicial.plusDays((long) (i - 1) * intervaloDias);

            Parcela p = new Parcela();
            p.setNumeroParcela(i);
            p.setTotalParcelas(numeroParcelas);
            p.setValorParcela(valorFinalParcela);
            p.setDataVencimento(dataVencimento);
            p.setStatus("PENDENTE");
            parcelas.add(p);
        }
        return parcelas;
    }
}