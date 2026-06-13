package com.joao.osMarmoraria.services;

import com.joao.osMarmoraria.domain.ContaReceber;
import com.joao.osMarmoraria.domain.Venda;
import com.joao.osMarmoraria.domain.enums.FormaPagamento;
import com.joao.osMarmoraria.repository.ContaReceberRepository;
import com.joao.osMarmoraria.services.exceptions.ContasReceberJaGeradasException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Teste de integração do faturamento de vendas ({@link FaturamentoService}),
 * a rede de segurança da matemática do dinheiro (Fase A, item 3 do ROADMAP).
 *
 * <p>Cobre a regra de parcelamento extraída na Fase 4 do REFACTORING: número de
 * parcelas conforme a forma de pagamento, divisão do valor com o resto na última
 * parcela, vencimentos a cada 30 dias, e a proteção contra geração duplicada.
 * É contra a entidade {@link ContaReceber} que essas garantias importam — daí a
 * integração com H2, não um teste de unidade da aritmética isolada.</p>
 */
@SpringBootTest
@ActiveProfiles("h2")
@Transactional
class FaturamentoServiceIntegrationTest {

    @Autowired
    private FaturamentoService faturamentoService;

    @Autowired
    private ContaReceberRepository contaReceberRepository;

    @PersistenceContext
    private EntityManager em;

    @Test
    void gerarParcelas_distribuiOValorComORestoNaUltimaENosVencimentosCertos() {
        // 1000 / 3 = 333,33 (arredonda p/ baixo); o centavo restante vai na última.
        Venda venda = seedVendaEfetivada(new BigDecimal("1000.00"), BigDecimal.ZERO,
                FormaPagamento.BOLETO_BANCARIO, 3);

        faturamentoService.gerarContasReceberParceladas(venda.getVenId());

        List<ContaReceber> contas = contaReceberRepository.findByVenda(venda);
        assertEquals(3, contas.size(), "deveria gerar 3 parcelas");

        BigDecimal soma = contas.stream()
                .map(ContaReceber::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(0, soma.compareTo(new BigDecimal("1000.00")), "a soma das parcelas deve fechar o total");

        List<BigDecimal> valores = contas.stream()
                .map(ContaReceber::getValor)
                .sorted()
                .collect(Collectors.toList());
        assertEquals(0, valores.get(0).compareTo(new BigDecimal("333.33")));
        assertEquals(0, valores.get(1).compareTo(new BigDecimal("333.33")));
        assertEquals(0, valores.get(2).compareTo(new BigDecimal("333.34")), "o resto entra na última parcela");

        LocalDate hoje = LocalDate.now();
        Set<LocalDate> vencimentos = contas.stream()
                .map(ContaReceber::getDataVencimento)
                .collect(Collectors.toSet());
        assertTrue(vencimentos.contains(hoje.plusDays(30)));
        assertTrue(vencimentos.contains(hoje.plusDays(60)));
        assertTrue(vencimentos.contains(hoje.plusDays(90)));

        assertTrue(contas.stream().allMatch(c -> "PENDENTE".equals(c.getStatus())));
    }

    @Test
    void formaSemParcelamento_geraUmaUnicaContaIgnorandoNumeroDeParcelas() {
        // DINHEIRO não permite parcelamento: mesmo pedindo 3, deve sair 1 conta.
        Venda venda = seedVendaEfetivada(new BigDecimal("500.00"), BigDecimal.ZERO,
                FormaPagamento.DINHEIRO, 3);

        faturamentoService.gerarContasReceberParceladas(venda.getVenId());

        List<ContaReceber> contas = contaReceberRepository.findByVenda(venda);
        assertEquals(1, contas.size());
        assertEquals(0, contas.get(0).getValor().compareTo(new BigDecimal("500.00")));
    }

    @Test
    void descontoEhAbatidoAntesDeParcelar() {
        // (1000 - 100) / 2 = 450,00 por parcela.
        Venda venda = seedVendaEfetivada(new BigDecimal("1000.00"), new BigDecimal("100.00"),
                FormaPagamento.BOLETO_BANCARIO, 2);

        faturamentoService.gerarContasReceberParceladas(venda.getVenId());

        List<ContaReceber> contas = contaReceberRepository.findByVenda(venda);
        assertEquals(2, contas.size());
        assertTrue(contas.stream().allMatch(c -> c.getValor().compareTo(new BigDecimal("450.00")) == 0));
    }

    @Test
    void gerarDuasVezes_lancaContasReceberJaGeradas() {
        Venda venda = seedVendaEfetivada(new BigDecimal("300.00"), BigDecimal.ZERO,
                FormaPagamento.BOLETO_BANCARIO, 2);

        faturamentoService.gerarContasReceberParceladas(venda.getVenId());

        assertThrows(ContasReceberJaGeradasException.class,
                () -> faturamentoService.gerarContasReceberParceladas(venda.getVenId()));
    }

    @Test
    void vendaNaoEfetivada_naoFatura() {
        Venda venda = seedVendaEfetivada(new BigDecimal("300.00"), BigDecimal.ZERO,
                FormaPagamento.BOLETO_BANCARIO, 2);
        venda.setDataFechamento(null); // desfaz a efetivação
        em.flush();

        assertThrows(IllegalStateException.class,
                () -> faturamentoService.gerarContasReceberParceladas(venda.getVenId()));
    }

    // ---- helper de seed ----

    private Venda seedVendaEfetivada(BigDecimal total, BigDecimal desconto,
                                     FormaPagamento formaPagamento, int numeroParcelas) {
        Venda venda = new Venda();
        venda.setTotal(total);
        venda.setDesconto(desconto);
        venda.setFormaPagamento(formaPagamento);
        venda.setNumeroParcelas(numeroParcelas);
        venda.setDataFechamento(new Date()); // efetivada
        em.persist(venda);
        return venda;
    }
}
