package com.joao.osMarmoraria.services;

import com.joao.osMarmoraria.domain.*;
import com.joao.osMarmoraria.domain.enums.TipoMovimentacao;
import com.joao.osMarmoraria.repository.*;
import com.joao.osMarmoraria.services.exceptions.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class EstoqueService {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private MovimentacaoEstoqueRepository movimentacaoRepository;

    @Autowired
    private EstoqueReservadoRepository estoqueReservadoRepository;

    @Autowired
    private ProjetoItemRepository projetoItemRepository;

    @Autowired
    private ItemOrdemServicoRepository itemOrdemServicoRepository;

    // Métodos de Reserva de Estoque

    @Transactional
    public void reservarMaterialParaVenda(Integer vendaId, Integer projetoId) {
        if (projetoId == null) {
            throw new IllegalArgumentException("Projeto ID é obrigatório para reserva de materiais");
        }

        // Verificar se já existe reserva para esta venda
        if (estoqueReservadoRepository.existsByVendaIdAndAtivoTrue(vendaId)) {
            throw new IllegalStateException("Já existem materiais reservados para esta venda");
        }

        List<ProjetoItem> itens = projetoItemRepository.findByProjetoId(projetoId);

        for (ProjetoItem item : itens) {
            Produto produto = produtoRepository.findById(item.getProdutoId())
                    .orElseThrow(() -> new ObjectNotFoundException("Produto não encontrado: " + item.getProdutoId()));

            BigDecimal quantidadeNecessaria = item.getQuantidade();
            BigDecimal estoqueDisponivel = calcularEstoqueDisponivel(produto.getProdId());

            if (estoqueDisponivel.compareTo(quantidadeNecessaria) < 0) {
                throw new IllegalStateException(
                        String.format("Estoque insuficiente para o produto %s. Disponível: %s, Necessário: %s",
                                produto.getNome(), estoqueDisponivel, quantidadeNecessaria)
                );
            }

            // Criar reserva
            EstoqueReservado reserva = new EstoqueReservado(
                    produto,
                    quantidadeNecessaria,
                    vendaId,
                    projetoId,
                    "Reserva automática para venda de projeto"
            );
            estoqueReservadoRepository.save(reserva);

            // Registrar movimentação de reserva
            registrarMovimentacao(
                    produto,
                    TipoMovimentacao.RESERVA_VENDA,
                    quantidadeNecessaria,
                    "Reserva para venda ID: " + vendaId + ", Projeto ID: " + projetoId,
                    vendaId,
                    null,
                    projetoId
            );
        }
    }

    @Transactional
    public void liberarReservaVenda(Integer vendaId) {
        List<EstoqueReservado> reservas = estoqueReservadoRepository.findByVendaIdAndAtivoTrue(vendaId);

        for (EstoqueReservado reserva : reservas) {
            reserva.liberar();
            estoqueReservadoRepository.save(reserva);

            // Registrar movimentação de liberação
            registrarMovimentacao(
                    reserva.getProduto(),
                    TipoMovimentacao.LIBERACAO_RESERVA,
                    reserva.getQuantidade(),
                    "Liberação de reserva da venda ID: " + vendaId,
                    vendaId,
                    null,
                    reserva.getProjetoId()
            );
        }
    }

    // Métodos de Baixa de Estoque

    @Transactional
    public void baixarEstoqueOrdemServico(Integer ordemServicoId) {
        List<ItemOrdemServico> itens = itemOrdemServicoRepository.findByOrdemServicoIdWithProduto(ordemServicoId);

        for (ItemOrdemServico item : itens) {
            Produto produto = item.getProduto();
            BigDecimal quantidade = item.getQuantidade();

            // Verificar se há reserva ativa para este produto nesta OS
            List<EstoqueReservado> reservas = estoqueReservadoRepository.findByOrdemServicoIdAndAtivoTrue(ordemServicoId);
            EstoqueReservado reservaItem = reservas.stream()
                    .filter(r -> r.getProduto().getProdId().equals(produto.getProdId()))
                    .findFirst()
                    .orElse(null);

            BigDecimal estoqueAnterior = produto.getEstoque();

            if (reservaItem != null) {
                // Se há reserva, apenas libera a reserva e baixa do estoque físico
                reservaItem.liberar();
                estoqueReservadoRepository.save(reservaItem);

                produto.baixarEstoque(quantidade);
                produtoRepository.save(produto);

                registrarMovimentacao(
                        produto,
                        TipoMovimentacao.BAIXA_ORDEM_SERVICO,
                        quantidade,
                        "Baixa por conclusão da OS ID: " + ordemServicoId + " (com reserva prévia)",
                        null,
                        ordemServicoId,
                        null
                );
            } else {
                // Se não há reserva, verifica disponibilidade e baixa diretamente
                BigDecimal estoqueDisponivel = calcularEstoqueDisponivel(produto.getProdId());

                if (estoqueDisponivel.compareTo(quantidade) < 0) {
                    throw new IllegalStateException(
                            String.format("Estoque insuficiente para o produto %s. Disponível: %s, Necessário: %s",
                                    produto.getNome(), estoqueDisponivel, quantidade)
                    );
                }

                produto.baixarEstoque(quantidade);
                produtoRepository.save(produto);

                registrarMovimentacao(
                        produto,
                        TipoMovimentacao.BAIXA_ORDEM_SERVICO,
                        quantidade,
                        "Baixa por conclusão da OS ID: " + ordemServicoId + " (sem reserva prévia)",
                        null,
                        ordemServicoId,
                        null
                );
            }
        }
    }

    // Métodos de Consulta

    @Transactional(readOnly = true)
    public BigDecimal calcularEstoqueDisponivel(Integer produtoId) {
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new ObjectNotFoundException("Produto não encontrado: " + produtoId));

        BigDecimal estoqueTotal = produto.getEstoque();
        BigDecimal estoqueReservado = estoqueReservadoRepository.calcularQuantidadeReservada(produtoId);

        return estoqueTotal.subtract(estoqueReservado);
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularEstoqueReservado(Integer produtoId) {
        return estoqueReservadoRepository.calcularQuantidadeReservada(produtoId);
    }

    @Transactional(readOnly = true)
    public List<MovimentacaoEstoque> obterHistoricoMovimentacao(Integer produtoId) {
        return movimentacaoRepository.findByProdutoProdIdOrderByDataMovimentacaoDesc(produtoId);
    }

    @Transactional(readOnly = true)
    public List<EstoqueReservado> obterReservasAtivas(Integer produtoId) {
        return estoqueReservadoRepository.findByProdutoProdIdAndAtivoTrueOrderByDataReservaDesc(produtoId);
    }

    // Métodos de Movimentação Manual

    @Transactional
    public void entradaEstoque(Integer produtoId, BigDecimal quantidade, String observacao, Integer usuarioId) {
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new ObjectNotFoundException("Produto não encontrado: " + produtoId));

        produto.aumentarEstoque(quantidade);
        produtoRepository.save(produto);

        registrarMovimentacao(
                produto,
                TipoMovimentacao.ENTRADA_AJUSTE,
                quantidade,
                observacao != null ? observacao : "Entrada manual de estoque",
                null,
                null,
                null
        );
    }

    @Transactional
    public void saidaEstoque(Integer produtoId, BigDecimal quantidade, String observacao, Integer usuarioId) {
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new ObjectNotFoundException("Produto não encontrado: " + produtoId));

        BigDecimal estoqueDisponivel = calcularEstoqueDisponivel(produtoId);

        if (estoqueDisponivel.compareTo(quantidade) < 0) {
            throw new IllegalStateException(
                    String.format("Estoque insuficiente. Disponível: %s, Solicitado: %s",
                            estoqueDisponivel, quantidade)
            );
        }

        produto.baixarEstoque(quantidade);
        produtoRepository.save(produto);

        registrarMovimentacao(
                produto,
                TipoMovimentacao.SAIDA_AJUSTE,
                quantidade,
                observacao != null ? observacao : "Saída manual de estoque",
                null,
                null,
                null
        );
    }

    // Método auxiliar para registrar movimentações

    private void registrarMovimentacao(Produto produto, TipoMovimentacao tipo, BigDecimal quantidade,
                                       String observacao, Integer vendaId, Integer ordemServicoId, Integer projetoId) {
        BigDecimal estoqueAnterior = produto.getEstoque();
        BigDecimal estoqueAtual;

        if (tipo.isEntrada()) {
            estoqueAtual = estoqueAnterior.add(quantidade);
        } else if (tipo.isSaida()) {
            estoqueAtual = estoqueAnterior.subtract(quantidade);
        } else {
            estoqueAtual = estoqueAnterior; // Para reservas, o estoque físico não muda
        }

        MovimentacaoEstoque movimentacao = new MovimentacaoEstoque(
                produto, tipo, quantidade, estoqueAnterior, estoqueAtual, observacao
        );

        movimentacao.setVendaId(vendaId);
        movimentacao.setOrdemServicoId(ordemServicoId);
        movimentacao.setProjetoId(projetoId);

        movimentacaoRepository.save(movimentacao);
    }

    // Métodos de Limpeza e Manutenção

    @Transactional
    public void limparReservasExpiradas() {
        List<EstoqueReservado> reservasExpiradas = estoqueReservadoRepository.findReservasExpiradas(LocalDateTime.now());

        for (EstoqueReservado reserva : reservasExpiradas) {
            reserva.liberar();
            estoqueReservadoRepository.save(reserva);

            registrarMovimentacao(
                    reserva.getProduto(),
                    TipoMovimentacao.LIBERACAO_RESERVA,
                    reserva.getQuantidade(),
                    "Liberação automática por expiração",
                    reserva.getVendaId(),
                    reserva.getOrdemServicoId(),
                    reserva.getProjetoId()
            );
        }
    }
}
