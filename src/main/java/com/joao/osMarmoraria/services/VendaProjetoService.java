package com.joao.osMarmoraria.services;

import com.joao.osMarmoraria.domain.Cliente;
import com.joao.osMarmoraria.domain.Projeto;
import com.joao.osMarmoraria.domain.Venda;
import com.joao.osMarmoraria.domain.enums.FormaPagamento;
import com.joao.osMarmoraria.domain.enums.StatusProjeto;
import com.joao.osMarmoraria.domain.enums.VendaTipo;
import com.joao.osMarmoraria.dtos.ContaReceberDTO;
import com.joao.osMarmoraria.dtos.OrdemServicoDTO;
import com.joao.osMarmoraria.dtos.VendaProjetoDTO;
import com.joao.osMarmoraria.mapper.VendaProjetoMapper;
import com.joao.osMarmoraria.repository.ClienteRepository;
import com.joao.osMarmoraria.repository.ProjetoRepository;
import com.joao.osMarmoraria.repository.VendaRepository;
import com.joao.osMarmoraria.services.exceptions.GeracaoDuplicadaException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Orquestra o ciclo de vida da venda de projeto sob encomenda: criação,
 * atualização, efetivação e a geração (idempotente) da ordem de serviço e
 * da conta a receber correspondentes.
 *
 * <p>Extraído do {@code VendaService}, que acumulava duas razões para
 * mudar: o fluxo de venda de produto de balcão ({@code VendaController}) e
 * o fluxo de venda de projeto ({@code VendaProjetoController}) — cada um
 * com seu próprio ciclo de vida e nenhuma sobreposição de chamadores (todo
 * consumidor desta classe é o {@code VendaProjetoController}; o
 * {@code VendaService} continua exclusivo do balcão). Relocação pura,
 * comportamento preservado byte a byte, incluindo pendências conhecidas:</p>
 * <ul>
 *   <li>{@code buscarVendaProjetoPorId} e os métodos que dependem dele
 *   inferem o projeto da venda por {@code clienteId + status VENDIDO} em
 *   vez de uma chave estrangeira direta — funciona apenas enquanto um
 *   cliente não tiver duas vendas de projeto simultâneas vendidas;</li>
 *   <li>{@code VendaProjetoMapper.podeGerarOS}/{@code podeGerarContaReceber}
 *   continuam calculados contra os defaults do DTO, nunca populados a
 *   partir do estado real da venda.</li>
 * </ul>
 * Nenhuma das duas foi corrigida aqui — é relocação de responsabilidade,
 * não correção de regra de negócio.
 */
@Service
@RequiredArgsConstructor
public class VendaProjetoService {

    private final VendaRepository vendaRepository;

    private final ClienteRepository clienteRepository;

    private final ProjetoRepository projetoRepository;

    private final ContaReceberService contaReceberService;

    private final OrdemServicoService ordemServicoService;

    private final VendaProjetoMapper vendaProjetoMapper;

    @Transactional
    public VendaProjetoDTO criarVendaProjeto(VendaProjetoDTO vendaProjetoDTO) {
        Cliente cliente = clienteRepository.findById(vendaProjetoDTO.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado! ID: " + vendaProjetoDTO.getClienteId()));

        Projeto projeto = projetoRepository.findById(vendaProjetoDTO.getProjetoId())
                .orElseThrow(() -> new RuntimeException("Projeto não encontrado! ID: " + vendaProjetoDTO.getProjetoId()));

        if (!"ORCAMENTO".equals(projeto.getStatus().name()) && !"APROVADO".equals(projeto.getStatus().name())) {
            throw new RuntimeException("Projeto deve estar em status 'ORÇAMENTO' ou 'APROVADO' para ser vendido");
        }

        Venda venda = new Venda();
        venda.setCliente(cliente);
        venda.setDataAbertura(new Date());
        venda.setTotal(vendaProjetoDTO.getValorTotal());
        venda.setDesconto(vendaProjetoDTO.getDesconto() != null ? vendaProjetoDTO.getDesconto() : BigDecimal.ZERO);
        venda.setVendaTipo(VendaTipo.ORCAMENTO);
        venda.setFormaPagamento(FormaPagamento.valueOf(vendaProjetoDTO.getFormaPagamento()));

        BigDecimal valorFinal = venda.getTotal().subtract(venda.getDesconto());
        venda.setTotal(valorFinal);

        venda = vendaRepository.save(venda);

        projeto.setStatus(StatusProjeto.VENDIDO);
        projetoRepository.save(projeto);

        return vendaProjetoMapper.toDto(venda, projeto);
    }

    @Transactional(readOnly = true)
    public VendaProjetoDTO buscarVendaProjetoPorId(Integer id) {
        Venda venda = findVendaById(id);

        List<Projeto> projetos = projetoRepository.findByClienteIdAndStatus(venda.getCliente().getCliId(), StatusProjeto.VENDIDO);
        Projeto projeto = projetos.isEmpty() ? null : projetos.get(0);

        return vendaProjetoMapper.toDto(venda, projeto);
    }

    @Transactional(readOnly = true)
    public List<VendaProjetoDTO> listarVendasProjetos() {
        List<Venda> vendas = vendaRepository.findByVendaTipo(VendaTipo.ORCAMENTO);
        return vendas.stream()
                .map(venda -> {
                    List<Projeto> projetos = projetoRepository.findByClienteIdAndStatus(venda.getCliente().getCliId(), StatusProjeto.VENDIDO);
                    Projeto projeto = projetos.isEmpty() ? null : projetos.get(0);
                    return vendaProjetoMapper.toDto(venda, projeto);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public VendaProjetoDTO atualizarVendaProjeto(VendaProjetoDTO vendaProjetoDTO) {
        Venda venda = findVendaById(vendaProjetoDTO.getId());

        venda.setTotal(vendaProjetoDTO.getValorTotal());
        venda.setDesconto(vendaProjetoDTO.getDesconto());
        venda.setFormaPagamento(FormaPagamento.valueOf(vendaProjetoDTO.getFormaPagamento()));

        BigDecimal valorFinal = venda.getTotal().subtract(venda.getDesconto());
        venda.setTotal(valorFinal);

        venda = vendaRepository.save(venda);

        List<Projeto> projetos = projetoRepository.findByClienteIdAndStatus(venda.getCliente().getCliId(), StatusProjeto.VENDIDO);
        Projeto projeto = projetos.isEmpty() ? null : projetos.get(0);

        return vendaProjetoMapper.toDto(venda, projeto);
    }

    @Transactional
    public VendaProjetoDTO efetuarVendaProjeto(Integer id) {
        Venda venda = findVendaById(id);
        venda.setDataFechamento(new Date());
        venda = vendaRepository.save(venda);

        List<Projeto> projetos = projetoRepository.findByClienteIdAndStatus(venda.getCliente().getCliId(), StatusProjeto.VENDIDO);
        Projeto projeto = projetos.isEmpty() ? null : projetos.get(0);

        return vendaProjetoMapper.toDto(venda, projeto);
    }

    @Transactional(readOnly = true)
    public List<VendaProjetoDTO> buscarVendasProjetosPorCliente(Integer clienteId) {
        List<Venda> vendas = vendaRepository.findByClienteIdAndVendaTipo(clienteId, VendaTipo.ORCAMENTO);
        return vendas.stream()
                .map(venda -> {
                    List<Projeto> projetos = projetoRepository.findByClienteIdAndStatus(clienteId, StatusProjeto.VENDIDO);
                    Projeto projeto = projetos.isEmpty() ? null : projetos.get(0);
                    return vendaProjetoMapper.toDto(venda, projeto);
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
            throw new GeracaoDuplicadaException("Ordem de serviço já foi gerada para esta venda");
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
            throw new GeracaoDuplicadaException("Conta a receber já foi gerada para esta venda");
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

    private Venda findVendaById(Integer id) {
        return vendaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venda não encontrada! ID: " + id));
    }
}
