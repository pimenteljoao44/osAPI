package com.joao.osMarmoraria.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joao.osMarmoraria.domain.Compra;
import com.joao.osMarmoraria.domain.ContaPagar;
import com.joao.osMarmoraria.domain.Parcela;
import com.joao.osMarmoraria.domain.PaymentTransaction;
import com.joao.osMarmoraria.domain.enums.PaymentMethod;
import com.joao.osMarmoraria.domain.enums.PaymentStatus;
import com.joao.osMarmoraria.domain.enums.StatusConta;
import com.joao.osMarmoraria.dtos.CompraDTO;
import com.joao.osMarmoraria.dtos.InstallmentRequestDTO;
import com.joao.osMarmoraria.gateway.dto.PaymentRequest;
import com.joao.osMarmoraria.repository.CompraRepository;
import com.joao.osMarmoraria.repository.ContaPagarRepository;
import com.joao.osMarmoraria.repository.ParcelaRepository;
import com.joao.osMarmoraria.repository.PaymentTransactionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@Transactional
class PaymentIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private ContaPagarRepository contaPagarRepository;

    @Autowired
    private ParcelaRepository parcelaRepository;

    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void testCompleteInstallmentPaymentFlow() throws Exception {
        // Step 1: Create a purchase with installments
        CompraDTO compraDTO = new CompraDTO();
        compraDTO.setObservacoes("Test Purchase with Installments");
        compraDTO.setValorTotal(new BigDecimal("1200.00"));
        compraDTO.setDataCompra(new Date());
        String compraJson = objectMapper.writeValueAsString(compraDTO);

        mockMvc.perform(post("/api/compras")
                .contentType(MediaType.APPLICATION_JSON)
                .content(compraJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.descricao").value("Test Purchase with Installments"))
                .andExpect(jsonPath("$.valorTotal").value(1200.00))
                .andExpect(jsonPath("$.installments").value(4));

        // Verify purchase was created
        List<Compra> compras = compraRepository.findAll();
        assertEquals(1, compras.size());
        Compra compra = compras.get(0);

        // Verify ContaPagar was created
        List<ContaPagar> contasPagar = contaPagarRepository.findAll();
        assertEquals(1, contasPagar.size());
        ContaPagar contaPagar = contasPagar.get(0);
        assertEquals(new BigDecimal("1200.00"), contaPagar.getValor());

        // Verify installments were created
        List<Parcela> parcelas = parcelaRepository.findByContaPagarIdOrderByNumeroParcela(contaPagar.getId());
        assertEquals(4, parcelas.size());

        // Verify installment values
        assertEquals(new BigDecimal("300.00"), parcelas.get(0).getValorParcela());
        assertEquals(new BigDecimal("300.00"), parcelas.get(1).getValorParcela());
        assertEquals(new BigDecimal("300.00"), parcelas.get(2).getValorParcela());
        assertEquals(new BigDecimal("300.00"), parcelas.get(3).getValorParcela());

        // Verify installment due dates
        assertEquals(LocalDate.now().plusDays(30), parcelas.get(0).getDataVencimento());
        assertEquals(LocalDate.now().plusDays(60), parcelas.get(1).getDataVencimento());
        assertEquals(LocalDate.now().plusDays(90), parcelas.get(2).getDataVencimento());
        assertEquals(LocalDate.now().plusDays(120), parcelas.get(3).getDataVencimento());

        // Step 2: Process payment for first installment
        Integer parcelaId = parcelas.get(0).getId();

        mockMvc.perform(post("/api/payment-gateway/payments/installments/" + parcelaId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.transactionId").exists())
                .andExpect(jsonPath("$.requestedAmount").value(300.00));

        // Verify payment transaction was created
        List<PaymentTransaction> transactions = paymentTransactionRepository.findAll();
        assertEquals(1, transactions.size());
        PaymentTransaction transaction = transactions.get(0);
        assertEquals(new BigDecimal("300.00"), transaction.getAmount());
        assertEquals("BRL", transaction.getCurrency());

        // Step 3: Query payment status
        String transactionId = transaction.getTransactionId();

        mockMvc.perform(get("/api/payment-gateway/payments/" + transactionId + "/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value(transactionId))
                .andExpect(jsonPath("$.amount").value(300.00))
                .andExpect(jsonPath("$.currency").value("BRL"));

        // Step 4: Get installment list
        mockMvc.perform(get("/api/parcelas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(4))
                .andExpect(jsonPath("$.content[0].numeroParcela").value(1))
                .andExpect(jsonPath("$.content[0].valor").value(300.00));

        // Step 5: Get installment summary
        mockMvc.perform(get("/api/parcelas/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pendingCount").value(4))
                .andExpect(jsonPath("$.pendingAmount").value(1200.00))
                .andExpect(jsonPath("$.paidCount").value(0))
                .andExpect(jsonPath("$.paidAmount").value(0.00));
    }

    @Test
    void testPaymentGatewayStatistics() throws Exception {
        // Create test transactions
        PaymentTransaction transaction1 = createTestTransaction("TXN_001", PaymentStatus.COMPLETED, new BigDecimal("100.00"));
        PaymentTransaction transaction2 = createTestTransaction("TXN_002", PaymentStatus.FAILED, new BigDecimal("200.00"));
        PaymentTransaction transaction3 = createTestTransaction("TXN_003", PaymentStatus.PENDING, new BigDecimal("150.00"));

        paymentTransactionRepository.saveAll(List.of(transaction1, transaction2, transaction3));

        // Get payment statistics
        mockMvc.perform(get("/api/payment-gateway/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionStatistics").isArray())
                .andExpect(jsonPath("$.paymentMethodStatistics").isArray())
                .andExpect(jsonPath("$.providerStatistics").isArray());
    }

    @Test
    void testInstallmentPaymentWithRefund() throws Exception {
        // Create installment
        InstallmentRequestDTO installmentRequest = new InstallmentRequestDTO();
        installmentRequest.setValorTotal(new BigDecimal("600.00"));


        // Create ContaPagar first
        ContaPagar contaPagar = new ContaPagar();
        contaPagar.setObservacoes("Test Account");
        contaPagar.setValor(new BigDecimal("600.00"));
        contaPagar.setStatus(StatusConta.PENDENTE.getDescricao());
        contaPagar.setDataVencimento(LocalDate.now().plusDays(30));
        contaPagar = contaPagarRepository.save(contaPagar);

        String requestJson = objectMapper.writeValueAsString(installmentRequest);

        // Create installments
        mockMvc.perform(post("/api/parcelas/conta-pagar/" + contaPagar.getId() + "/installments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].valor").value(300.00))
                .andExpect(jsonPath("$[1].valor").value(300.00));

        // Get created installments
        List<Parcela> parcelas = parcelaRepository.findByContaPagarIdOrderByNumeroParcela(contaPagar.getId());
        assertEquals(2, parcelas.size());

        // Process payment for first installment
        Integer parcelaId = parcelas.get(0).getId();

        mockMvc.perform(post("/api/payment-gateway/payments/installments/" + parcelaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Get the transaction
        List<PaymentTransaction> transactions = paymentTransactionRepository.findAll();
        assertEquals(1, transactions.size());
        PaymentTransaction transaction = transactions.get(0);

        // Simulate successful payment by updating status
        transaction.setStatus(PaymentStatus.COMPLETED);
        transaction.setProcessedAt(LocalDateTime.now());
        paymentTransactionRepository.save(transaction);

        // Process refund
        String refundJson = "{\"amount\": 150.00, \"reason\": \"Partial refund test\"}";

        mockMvc.perform(post("/api/payment-gateway/payments/" + transaction.getTransactionId() + "/refund")
                .contentType(MediaType.APPLICATION_JSON)
                .content(refundJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.originalTransactionId").value(transaction.getTransactionId()))
                .andExpect(jsonPath("$.refundedAmount").value(150.00));
    }

    @Test
    void testOverdueInstallmentsQuery() throws Exception {
        // Create overdue installment
        ContaPagar contaPagar = new ContaPagar();
        contaPagar.setObservacoes("Overdue Test Account");
        contaPagar.setValor(new BigDecimal("500.00"));
        contaPagar.setStatus(StatusConta.PENDENTE.getDescricao());
        contaPagar.setDataVencimento(LocalDate.now().minusDays(10)); // Overdue
        contaPagar = contaPagarRepository.save(contaPagar);

        Parcela overdueParcela = new Parcela();
        overdueParcela.setNumeroParcela(1);
        overdueParcela.setValorParcela(new BigDecimal("500.00"));
        overdueParcela.setDataVencimento(LocalDate.now().minusDays(10)); // Overdue
        overdueParcela.setStatus(StatusConta.PENDENTE.getDescricao());
        overdueParcela.setContaPagar(contaPagar);
        parcelaRepository.save(overdueParcela);

        // Query overdue installments
        mockMvc.perform(get("/api/parcelas/overdue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("VENCIDO"))
                .andExpect(jsonPath("$[0].valor").value(500.00));
    }

    @Test
    void testPaymentMethodAvailability() throws Exception {
        // Test getting available payment methods
        mockMvc.perform(get("/api/payment-gateway/payment-methods")
                .param("amount", "100.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // Test getting providers for specific payment method
        mockMvc.perform(get("/api/payment-gateway/providers")
                .param("paymentMethod", "PIX"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testTransactionFiltering() throws Exception {
        // Create transactions with different statuses
        PaymentTransaction completedTx = createTestTransaction("TXN_COMPLETED", PaymentStatus.COMPLETED, new BigDecimal("100.00"));
        PaymentTransaction failedTx = createTestTransaction("TXN_FAILED", PaymentStatus.FAILED, new BigDecimal("200.00"));
        PaymentTransaction pendingTx = createTestTransaction("TXN_PENDING", PaymentStatus.PENDING, new BigDecimal("150.00"));

        paymentTransactionRepository.saveAll(List.of(completedTx, failedTx, pendingTx));

        // Test filtering by status
        mockMvc.perform(get("/api/payment-gateway/transactions")
                .param("status", "COMPLETED")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].status").value("COMPLETED"));

        // Test filtering by payment method
        mockMvc.perform(get("/api/payment-gateway/transactions")
                .param("paymentMethod", "PIX")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testHealthCheck() throws Exception {
        mockMvc.perform(get("/api/payment-gateway/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testInstallmentPaymentFlow() throws Exception {
        // Create a purchase that generates installments
        CompraDTO compraDTO = new CompraDTO();
        compraDTO.setObservacoes("Installment Test Purchase");
        compraDTO.setValorTotal(new BigDecimal("900.00"));
        compraDTO.setDataCompra(new Date());


        String compraJson = objectMapper.writeValueAsString(compraDTO);

        mockMvc.perform(post("/api/compras")
                .contentType(MediaType.APPLICATION_JSON)
                .content(compraJson))
                .andExpect(status().isCreated());

        // Get the created installments
        List<Parcela> parcelas = parcelaRepository.findAll();
        assertEquals(3, parcelas.size());

        // Pay each installment
        for (Parcela parcela : parcelas) {
            mockMvc.perform(post("/api/payment-gateway/payments/installments/" + parcela.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.requestedAmount").value(300.00));
        }

        // Verify all transactions were created
        List<PaymentTransaction> transactions = paymentTransactionRepository.findAll();
        assertEquals(3, transactions.size());

        // Verify transaction amounts
        for (PaymentTransaction transaction : transactions) {
            assertEquals(new BigDecimal("300.00"), transaction.getAmount());
            assertEquals(PaymentMethod.PIX, transaction.getPaymentMethod()); // Default method
        }
    }

    private PaymentTransaction createTestTransaction(String transactionId, PaymentStatus status, BigDecimal amount) {
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setTransactionId(transactionId);
        transaction.setProviderId("test-provider");
        transaction.setPaymentMethod(PaymentMethod.PIX);
        transaction.setStatus(status);
        transaction.setAmount(amount);
        transaction.setCurrency("BRL");
        transaction.setDescription("Test transaction");
        transaction.setRetryCount(0);
        transaction.setMaxRetries(3);
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());
        
        if (status == PaymentStatus.COMPLETED) {
            transaction.setProcessedAt(LocalDateTime.now());
            transaction.setProcessedAmount(amount);
        }
        
        return transaction;
    }
}

