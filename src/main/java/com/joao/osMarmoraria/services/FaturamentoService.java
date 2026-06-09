package com.joao.osMarmoraria.services;

import com.joao.osMarmoraria.domain.ContaReceber;
import com.joao.osMarmoraria.domain.Parcela;
import com.joao.osMarmoraria.domain.Venda;
import com.joao.osMarmoraria.repository.ContaReceberRepository;
import com.joao.osMarmoraria.repository.VendaRepository;
import com.joao.osMarmoraria.services.exceptions.ContasReceberJaGeradasException;
import com.joao.osMarmoraria.services.exceptions.ObjectNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Faturamento de vendas: geração de contas a receber e respectivas parcelas.
 *
 * <p>Extraído do {@code VendaService} (Fase 4 do REFACTORING.md): a regra de
 * parcelamento — intervalo de vencimento, divisão do valor com resto na última
 * parcela — é uma razão de mudança própria, independente do ciclo de vida da
 * venda. Quem muda a política de cobrança mexe aqui; quem muda o fluxo de
 * venda não encosta nesta classe.</p>
 */
@Service
@RequiredArgsConstructor
public class FaturamentoService {

    /** Intervalo, em dias, entre o fechamento da venda e cada vencimento de parcela. */
    private static final int DIAS_ENTRE_PARCELAS = 30;

    private final VendaRepository vendaRepository;

    private final ContaReceberRepository contaReceberRepository;

    private final ParcelaService parcelaService;

    @Transactional
    public void gerarContasReceberParceladas(Integer vendaId) {
        Venda venda = vendaRepository.findById(vendaId)
                .orElseThrow(() -> new ObjectNotFoundException("Venda não encontrada! ID: " + vendaId));
        if (venda.getDataFechamento() == null) {
            throw new IllegalStateException("Venda deve estar efetivada para gerar contas a receber");
        }

        if (!contaReceberRepository.findByVenda(venda).isEmpty()) {
            throw new ContasReceberJaGeradasException(vendaId);
        }

        boolean permiteParcelamento = venda.getFormaPagamento().permiteParcelamento();
        Integer numeroParcelas = permiteParcelamento && venda.getNumeroParcelas() != null ? venda.getNumeroParcelas() : 1;
        BigDecimal valorTotal = venda.getTotal().subtract(venda.getDesconto() != null ? venda.getDesconto() : BigDecimal.ZERO);

        LocalDate primeiroVencimento = LocalDate.now().plusDays(DIAS_ENTRE_PARCELAS);
        List<Parcela> parcelas = gerarParcelas(valorTotal, numeroParcelas, primeiroVencimento, DIAS_ENTRE_PARCELAS);

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
}
