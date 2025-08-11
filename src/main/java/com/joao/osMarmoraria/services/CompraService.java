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
import java.util.stream.Collectors;

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
        compra.setValorTotal(compra.calculaTotal()); // Garante que o total seja calculado
        compra.setQuantidadeTotal(compra.calculaQuantidadeTotal()); // Garante que a quantidade total seja calculada

        Compra savedCompra = compraRepository.save(compra);

        // Associa os itens à compra salva e salva os itens
        for (ItemCompra item : savedCompra.getItensCompra()) {
            item.setCompra(savedCompra);
            itemRepository.save(item);
        }

        // Aumenta o estoque dos produtos
        for (ItemCompra item : savedCompra.getItensCompra()) {
            Produto produto = produtoRepository.findById(item.getProduto().getProdId())
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado! ID: " + item.getProduto().getProdId()));
            produto.aumentarEstoque(item.getQuantidade());
            produtoRepository.save(produto);
        }

        // Gerar contas a pagar (parceladas ou não) usando InstallmentRequestDTO se disponível
        if (objDto.getInstallmentRequest() != null && objDto.getInstallmentRequest().isValid()) {
            gerarContasPagarComInstallmentRequest(savedCompra.getComprId(), objDto.getInstallmentRequest());
        } else {
            gerarContasPagarParceladas(savedCompra.getComprId());
        }

        return savedCompra;
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
        if (objDto.getItensCompra() != null) {
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
        }

        Compra compra = new Compra(
                objDto.getComprId(),
                objDto.getObservacoes(),
                objDto.getDataCompra() != null ? objDto.getDataCompra() : new Date(),
                fornecedor.get(),
                funcionario.get(),
                FormaPagamento.toEnum(objDto.getFormaPagamento()),
                items
        );

        // Setar campos de parcelamento
        compra.setNumeroParcelas(objDto.getNumeroParcelas() != null ? objDto.getNumeroParcelas() : 1);
        compra.setIntervaloParcelas(objDto.getIntervaloParcelas() != null ? objDto.getIntervaloParcelas() : 30);

        return compra;
    }

    @Transactional
    public void gerarContasPagarParceladas(Integer compraId) {
        Compra compra = compraRepository.findById(compraId)
                .orElseThrow(() -> new ObjectNotFoundException("Compra não encontrada com ID: " + compraId));

        List<ContaPagar> contasExistentes = contaPagarService.buscarPorCompra(compraId);
        if (!contasExistentes.isEmpty()) {
            throw new IllegalStateException("Contas a pagar já foram geradas para esta compra");
        }

        // Verificar se a forma de pagamento permite parcelamento
        boolean permiteParcelamento = compra.getFormaPagamento().permiteParcelamento();
        Integer numeroParcelas = permiteParcelamento ?
                (compra.getNumeroParcelas() != null ? compra.getNumeroParcelas() : 1) : 1;

        Integer intervaloParcelas = compra.getIntervaloParcelas() != null ? compra.getIntervaloParcelas() : 30;
        BigDecimal valorTotal = compra.getValorTotal();

        if (numeroParcelas == 1) {
            // Pagamento à vista - uma única conta a pagar
            ContaPagar conta = new ContaPagar();
            conta.setCompra(compra);
            conta.setDescricao("Compra #" + compra.getComprId() + " - Pagamento à vista");
            conta.setValor(valorTotal);

            LocalDate dataVencimento = LocalDate.now().plusDays(7); // 7 dias para pagamento
            conta.setDataVencimento(dataVencimento);
            conta.setStatus("PENDENTE");

            ContaPagarDTO contaDTO = new ContaPagarDTO(conta);
            contaPagarService.criar(contaDTO);

        } else {
            // Pagamento parcelado - múltiplas contas a pagar com parcelas
            List<Parcela> parcelas = gerarParcelasCompra(valorTotal, numeroParcelas, LocalDate.now().plusDays(30), intervaloParcelas);

            for (Parcela parcela : parcelas) {
                ContaPagar conta = new ContaPagar();
                conta.setCompra(compra);
                conta.setDescricao("Compra #" + compra.getComprId() + " - Parcela " + parcela.getNumeroParcela() + "/" + numeroParcelas);
                conta.setValor(parcela.getValorParcela());
                conta.setDataVencimento(parcela.getDataVencimento());
                conta.setStatus("PENDENTE");

                ContaPagarDTO contaDTO = new ContaPagarDTO(conta);
                ContaPagar contaSalva = contaPagarService.criar(contaDTO);

                // Associar parcela à conta a pagar
                parcela.setContaPagar(contaSalva);
                parcelaService.salvar(parcela);
            }
        }
    }

    /**
     * Gera contas a pagar usando dados do InstallmentRequestDTO
     */
    @Transactional
    public void gerarContasPagarComInstallmentRequest(Integer compraId, InstallmentRequestDTO installmentRequest) {
        Compra compra = compraRepository.findById(compraId)
                .orElseThrow(() -> new ObjectNotFoundException("Compra não encontrada com ID: " + compraId));

        List<ContaPagar> contasExistentes = contaPagarService.buscarPorCompra(compraId);
        if (!contasExistentes.isEmpty()) {
            throw new IllegalStateException("Contas a pagar já foram geradas para esta compra");
        }

        Integer numeroParcelas = installmentRequest.getNumeroParcelas();
        BigDecimal valorTotal = installmentRequest.getValorTotal();
        LocalDate dataPrimeiroVencimento = installmentRequest.getDataPrimeiroVencimento();
        Integer intervaloDias = installmentRequest.getIntervaloDias();

        if (numeroParcelas == 1) {
            // Pagamento à vista - uma única conta a pagar
            ContaPagar conta = new ContaPagar();
            conta.setCompra(compra);
            conta.setDescricao("Compra #" + compra.getComprId() + " - Pagamento à vista");
            conta.setValor(valorTotal);
            conta.setDataVencimento(dataPrimeiroVencimento);
            conta.setStatus("PENDENTE");

            ContaPagarDTO contaDTO = new ContaPagarDTO(conta);
            contaPagarService.criar(contaDTO);

        } else {
            // Pagamento parcelado - múltiplas contas a pagar com parcelas
            List<Parcela> parcelas = gerarParcelasCompra(valorTotal, numeroParcelas, dataPrimeiroVencimento, intervaloDias);

            for (Parcela parcela : parcelas) {
                ContaPagar conta = new ContaPagar();
                conta.setCompra(compra);
                conta.setDescricao("Compra #" + compra.getComprId() + " - Parcela " + parcela.getNumeroParcela() + "/" + numeroParcelas);
                conta.setValor(parcela.getValorParcela());
                conta.setDataVencimento(parcela.getDataVencimento());
                conta.setStatus("PENDENTE");

                ContaPagarDTO contaDTO = new ContaPagarDTO(conta);
                ContaPagar contaSalva = contaPagarService.criar(contaDTO);

                // Associar parcela à conta a pagar
                parcela.setContaPagar(contaSalva);
                parcelaService.salvar(parcela);
            }
        }
    }

    /**
     * Gera as parcelas para uma compra
     */
    private List<Parcela> gerarParcelasCompra(BigDecimal valorTotal, int numeroParcelas, LocalDate dataVencimentoInicial, int intervaloDias) {
        List<Parcela> parcelas = new ArrayList<>();

        // Calcula o valor base de cada parcela
        BigDecimal valorParcela = valorTotal.divide(BigDecimal.valueOf(numeroParcelas), 2, BigDecimal.ROUND_DOWN);

        // Calcula o resto para ajustar na última parcela
        BigDecimal valorRestante = valorTotal.subtract(valorParcela.multiply(BigDecimal.valueOf(numeroParcelas)));

        for (int i = 1; i <= numeroParcelas; i++) {
            BigDecimal valorFinal = valorParcela;

            // Adiciona o resto na última parcela
            if (i == numeroParcelas) {
                valorFinal = valorFinal.add(valorRestante);
            }

            LocalDate dataVencimento = dataVencimentoInicial.plusDays((long) (i - 1) * intervaloDias);

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
}