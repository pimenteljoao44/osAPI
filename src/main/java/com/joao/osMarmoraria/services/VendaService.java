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
import java.math.RoundingMode;
import java.time.LocalDate;
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
    private ContaReceberService contaReceberService;

    @Autowired
    private OrdemServicoService ordemServicoService;

    @Autowired
    private ParcelaService parcelaService;

    @Autowired
    private ContaReceberRepository contaReceberRepository;

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

        // Se a venda já está efetivada e tem dados de parcelamento, gerar contas a receber
        if (savedVenda.getDataFechamento() != null && objDto.getInstallmentRequest() != null && objDto.getInstallmentRequest().isValid()) {
            gerarContasReceberComInstallmentRequest(savedVenda.getVenId(), objDto.getInstallmentRequest());
        }

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
    public void gerarContasReceberParceladas(Integer vendaId) {
        Venda venda = vendaRepository.findById(vendaId)
                .orElseThrow(() -> new ObjectNotFoundException("Venda não encontrada com ID: " + vendaId));

        if (venda.getDataFechamento() == null) {
            throw new IllegalStateException("Venda deve estar efetivada para gerar contas a receber");
        }

        List<ContaReceber> contasExistentes = contaReceberRepository.findByVenda(venda);
        if (!contasExistentes.isEmpty()) {
            throw new IllegalStateException("Contas a receber já foram geradas para esta venda");
        }

        Integer numeroParcelas = venda.getNumeroParcelas() != null ? venda.getNumeroParcelas() : 1;
        BigDecimal valorTotal = venda.getTotal();
        BigDecimal valorParcelaCalculado = valorTotal.divide(BigDecimal.valueOf(numeroParcelas), 2, RoundingMode.HALF_UP);

        BigDecimal somaParcelasAnteriores = valorParcelaCalculado.multiply(BigDecimal.valueOf(numeroParcelas - 1));
        BigDecimal ultimaParcela = valorTotal.subtract(somaParcelasAnteriores);

        LocalDate dataVencimento = venda.getDataFechamento().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        for (int i = 1; i <= numeroParcelas; i++) {
            ContaReceber conta = new ContaReceber();
            conta.setVenda(venda);
            conta.setDescricao("Venda #" + venda.getVenId() + " - Parcela " + i + "/" + numeroParcelas);

            BigDecimal valorDaParcela = (i == numeroParcelas) ? ultimaParcela : valorParcelaCalculado;
            conta.setValor(valorDaParcela);

            conta.setDataVencimento(dataVencimento);
            conta.setStatus("PENDENTE");
            conta.setDataCriacao(LocalDate.now());

            contaReceberRepository.save(conta);

            dataVencimento = dataVencimento.plusDays(30);
        }
    }

    @Transactional
    public void gerarContasReceberComInstallmentRequest(Integer vendaId, InstallmentRequestDTO installmentRequest) {
        Venda venda = vendaRepository.findById(vendaId)
                .orElseThrow(() -> new ObjectNotFoundException("Venda não encontrada com ID: " + vendaId));

        if (venda.getDataFechamento() == null) {
            throw new IllegalStateException("Venda deve estar efetivada para gerar contas a receber");
        }

        List<ContaReceber> contasExistentes = contaReceberRepository.findByVenda(venda);
        if (!contasExistentes.isEmpty()) {
            throw new IllegalStateException("Contas a receber já foram geradas para esta venda");
        }

        Integer numeroParcelas = installmentRequest.getNumeroParcelas();
        BigDecimal valorTotal = installmentRequest.getValorTotal();
        LocalDate dataPrimeiroVencimento = installmentRequest.getDataPrimeiroVencimento();
        Integer intervaloDias = installmentRequest.getIntervaloDias();

        if (numeroParcelas == 1) {
            // Pagamento à vista - uma única conta a receber
            ContaReceber conta = new ContaReceber();
            conta.setVenda(venda);
            conta.setDescricao("Venda #" + venda.getVenId() + " - Pagamento à vista");
            conta.setValor(valorTotal);
            conta.setDataVencimento(dataPrimeiroVencimento);
            conta.setStatus("PENDENTE");
            conta.setDataCriacao(LocalDate.now());

            contaReceberRepository.save(conta);

        } else {
            // Pagamento parcelado - múltiplas contas a receber
            BigDecimal valorParcelaCalculado = valorTotal.divide(BigDecimal.valueOf(numeroParcelas), 2, RoundingMode.HALF_UP);
            BigDecimal somaParcelasAnteriores = valorParcelaCalculado.multiply(BigDecimal.valueOf(numeroParcelas - 1));
            BigDecimal ultimaParcela = valorTotal.subtract(somaParcelasAnteriores);

            LocalDate dataVencimento = dataPrimeiroVencimento;

            for (int i = 1; i <= numeroParcelas; i++) {
                ContaReceber conta = new ContaReceber();
                conta.setVenda(venda);
                conta.setDescricao("Venda #" + venda.getVenId() + " - Parcela " + i + "/" + numeroParcelas);

                BigDecimal valorDaParcela = (i == numeroParcelas) ? ultimaParcela : valorParcelaCalculado;
                conta.setValor(valorDaParcela);

                conta.setDataVencimento(dataVencimento);
                conta.setStatus("PENDENTE");
                conta.setDataCriacao(LocalDate.now());

                contaReceberRepository.save(conta);

                dataVencimento = dataVencimento.plusDays(intervaloDias);
            }
        }
    }


    public void efetivarVenda(Integer vendaId) {
        Venda venda = findById(vendaId);
        venda.efetuarVenda(); // Atualiza a data de fechamento e recalcula o total

        vendaRepository.save(venda);
    }


    @Transactional
    public Map<String, Object> processarVendaCompleta(Integer vendaId) {
        Map<String, Object> resultado = new HashMap<>();
        List<String> erros = new ArrayList<>();
        List<String> sucessos = new ArrayList<>();

        try {
            Venda venda = findById(vendaId);

            // 1. Efetivar a venda se ainda não foi efetivada
            if (venda.getDataFechamento() == null) {
                efetivarVenda(vendaId);
                sucessos.add("Venda efetivada com sucesso");
            }

            // 2. Gerar contas a receber parceladas
            try {
                gerarContasReceberParceladas(vendaId);
                sucessos.add("Contas a receber geradas com sucesso");
            } catch (IllegalStateException e) {
                if (e.getMessage().contains("já foram geradas")) {
                    sucessos.add("Contas a receber já existem");
                } else {
                    erros.add("Erro ao gerar contas a receber: " + e.getMessage());
                }
            }

            // 3. Gerar ordem de serviço
            try {
                String resultadoOS = gerarOrdemServicoParaVenda(vendaId);
                sucessos.add(resultadoOS);
            } catch (Exception e) {
                erros.add("Erro ao gerar ordem de serviço: " + e.getMessage());
            }

            // Determinar resultado final
            boolean success = erros.isEmpty();
            resultado.put("success", success);
            resultado.put("sucessos", sucessos);
            resultado.put("erros", erros);

            if (success) {
                resultado.put("message", "Venda processada completamente com sucesso");
            } else {
                resultado.put("message", "Venda processada com alguns problemas: " + String.join(", ", erros));
            }

        } catch (Exception e) {
            resultado.put("success", false);
            resultado.put("message", "Erro geral no processamento: " + e.getMessage());
            resultado.put("erros", Arrays.asList(e.getMessage()));
        }

        return resultado;
    }

    @Transactional
    public Map<String, Object> processarVendaCompleta(Integer vendaId, InstallmentRequestDTO installmentRequest) {
        Map<String, Object> resultado = new HashMap<>();
        List<String> erros = new ArrayList<>();
        List<String> sucessos = new ArrayList<>();

        try {
            Venda venda = findById(vendaId);

            // 1. Efetivar a venda se ainda não foi efetivada
            if (venda.getDataFechamento() == null) {
                efetivarVenda(vendaId);
                sucessos.add("Venda efetivada com sucesso");
            }

            // 2. Gerar contas a receber usando InstallmentRequestDTO se fornecido
            try {
                if (installmentRequest != null && installmentRequest.isValid()) {
                    gerarContasReceberComInstallmentRequest(vendaId, installmentRequest);
                    sucessos.add("Contas a receber geradas com sucesso usando dados de parcelamento");
                } else {
                    gerarContasReceberParceladas(vendaId);
                    sucessos.add("Contas a receber geradas com sucesso");
                }
            } catch (IllegalStateException e) {
                if (e.getMessage().contains("já foram geradas")) {
                    sucessos.add("Contas a receber já existem");
                } else {
                    erros.add("Erro ao gerar contas a receber: " + e.getMessage());
                }
            }

            // 3. Gerar ordem de serviço
            try {
                String resultadoOS = gerarOrdemServicoParaVenda(vendaId);
                sucessos.add(resultadoOS);
            } catch (Exception e) {
                erros.add("Erro ao gerar ordem de serviço: " + e.getMessage());
            }

            // Determinar resultado final
            boolean success = erros.isEmpty();
            resultado.put("success", success);
            resultado.put("sucessos", sucessos);
            resultado.put("erros", erros);

            if (success) {
                resultado.put("message", "Venda processada completamente com sucesso");
            } else {
                resultado.put("message", "Venda processada com alguns problemas: " + String.join(", ", erros));
            }

        } catch (Exception e) {
            resultado.put("success", false);
            resultado.put("message", "Erro geral no processamento: " + e.getMessage());
            resultado.put("erros", Arrays.asList(e.getMessage()));
        }

        return resultado;
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
            return "Venda realizada com sucesso";
        } else {
            throw new IllegalStateException("Tipo de venda não suportado para geração de ordem de serviço");
        }
    }

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
        try {
            Venda venda = findById(vendaId);

            if (venda.getDataFechamento() == null) {
                throw new IllegalStateException("Venda deve estar efetivada para gerar ordem de serviço");
            }

            if (!venda.isVendaProjeto()) {
                throw new IllegalStateException("Venda não é do tipo projeto");
            }

            if (venda.getProjetoId() == null) {
                throw new IllegalStateException("Projeto não associado à venda");
            }

            if (ordemServicoService.existeOrdemServicoParaProjeto(venda.getProjetoId())) {
                throw new IllegalStateException("Já existe ordem de serviço para este projeto");
            }

            OrdemServicoDTO os = ordemServicoService.gerarPorProjeto(venda.getProjetoId());
            return "Ordem de serviço " + os.getNumero() + " gerada com sucesso";

        } catch (Exception e) {
            throw new RuntimeException("Falha ao gerar ordem de serviço: " + e.getMessage());
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