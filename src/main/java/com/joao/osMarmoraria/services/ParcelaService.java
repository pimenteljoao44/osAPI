package com.joao.osMarmoraria.services;

import com.joao.osMarmoraria.domain.*;
import com.joao.osMarmoraria.dtos.InstallmentRequestDTO;
import com.joao.osMarmoraria.dtos.ParcelaDTO;
import com.joao.osMarmoraria.repository.ParcelaRepository;
import com.joao.osMarmoraria.services.exceptions.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ParcelaService {

    @Autowired
    private ParcelaRepository parcelaRepository;

    /**
     * Gera parcelas a partir de uma ContaReceber já existente.
     * Usado pelo VendaService para criar o parcelamento de uma venda.
     */
    public List<ParcelaDTO> gerarParcelasParaContaReceber(ContaReceber contaReceber, InstallmentRequestDTO request) {
        validateInstallmentRequest(request);

        List<Parcela> parcelas = new ArrayList<>();
        LocalDate dataVencimento = request.getDataPrimeiroVencimento();

        for (int i = 1; i <= request.getNumeroParcelas(); i++) {
            BigDecimal valorParcela = calculateInstallmentAmount(request.getValorTotal(), request.getNumeroParcelas(), i);

            Parcela parcela = new Parcela();
            parcela.setNumeroParcela(i);
            parcela.setTotalParcelas(request.getNumeroParcelas());
            parcela.setValorParcela(valorParcela);
            parcela.setDataVencimento(dataVencimento);
            parcela.setStatus("PENDENTE");
            parcela.setObservacoes(request.getObservacoes());
            parcela.setContaReceber(contaReceber); // Associa a parcela à conta principal

            parcelas.add(parcela);
            // Avança para a data da próxima parcela
            dataVencimento = dataVencimento.plusDays(request.getIntervaloDias() != null ? request.getIntervaloDias() : 30);
        }

        List<Parcela> parcelasSalvas = parcelaRepository.saveAll(parcelas);
        return parcelasSalvas.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // CRUD Operations
    public List<ParcelaDTO> listarTodas() {
        return parcelaRepository.findAllWithDetails().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ParcelaDTO buscarPorId(Integer id) {
        Parcela parcela = parcelaRepository.findByIdWithDetails(id);
        if (parcela == null) {
            throw new ObjectNotFoundException("Parcela não encontrada com ID: " + id);
        }
        return convertToDTO(parcela);
    }

    public ParcelaDTO atualizar(Integer id, ParcelaDTO parcelaDTO) {
        Parcela parcela = parcelaRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Parcela não encontrada com ID: " + id));

        parcela.setValorParcela(parcelaDTO.getValorParcela());
        parcela.setDataVencimento(parcelaDTO.getDataVencimento());
        parcela.setObservacoes(parcelaDTO.getObservacoes());

        parcela = parcelaRepository.save(parcela);
        return convertToDTO(parcela);
    }

    public void deletar(Integer id) {
        Parcela parcela = parcelaRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Parcela não encontrada com ID: " + id));

        if (parcela.isPaga()) {
            throw new IllegalStateException("Não é possível excluir uma parcela já paga");
        }

        parcelaRepository.delete(parcela);
    }

    // Installment Generation Methods
    public List<ParcelaDTO> gerarParcelasParaCompra(Compra compra, InstallmentRequestDTO request) {
        validateInstallmentRequest(request);

        List<Parcela> parcelas = new ArrayList<>();
        LocalDate dataVencimento = request.getDataPrimeiroVencimento();

        for (int i = 1; i <= request.getNumeroParcelas(); i++) {
            BigDecimal valorParcela = calculateInstallmentAmount(request.getValorTotal(), request.getNumeroParcelas(), i);

            Parcela parcela = new Parcela();
            parcela.setNumeroParcela(i);
            parcela.setTotalParcelas(request.getNumeroParcelas());
            parcela.setValorParcela(valorParcela);
            parcela.setDataVencimento(dataVencimento);
            parcela.setStatus("PENDENTE");
            parcela.setObservacoes(request.getObservacoes());

            if (!compra.getContasPagar().isEmpty()) {
                parcela.setContaPagar(compra.getContasPagar().get(0));
            }

            parcelas.add(parcela);
            dataVencimento = dataVencimento.plusDays(request.getIntervaloDias());
        }

        List<Parcela> parcelasSalvas = parcelaRepository.saveAll(parcelas);
        return parcelasSalvas.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<ParcelaDTO> gerarParcelasParaVenda(Venda venda, InstallmentRequestDTO request) {
        validateInstallmentRequest(request);

        List<Parcela> parcelas = new ArrayList<>();
        LocalDate dataVencimento = request.getDataPrimeiroVencimento();

        for (int i = 1; i <= request.getNumeroParcelas(); i++) {
            BigDecimal valorParcela = calculateInstallmentAmount(request.getValorTotal(), request.getNumeroParcelas(), i);

            Parcela parcela = new Parcela();
            parcela.setNumeroParcela(i);
            parcela.setTotalParcelas(request.getNumeroParcelas());
            parcela.setValorParcela(valorParcela);
            parcela.setDataVencimento(dataVencimento);
            parcela.setStatus("PENDENTE");
            parcela.setObservacoes(request.getObservacoes());

            if (venda.getContasReceber() != null && !venda.getContasReceber().isEmpty()) {
                parcela.setContaReceber(venda.getContasReceber().get(0));
            }

            parcelas.add(parcela);
            dataVencimento = dataVencimento.plusDays(request.getIntervaloDias());
        }

        List<Parcela> parcelasSalvas = parcelaRepository.saveAll(parcelas);
        return parcelasSalvas.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Payment Processing Methods
    public ParcelaDTO marcarParcelaComoPaga(Integer parcelaId, LocalDate dataPagamento) {
        Parcela parcela = parcelaRepository.findById(parcelaId)
                .orElseThrow(() -> new ObjectNotFoundException("Parcela não encontrada com ID: " + parcelaId));

        if (parcela.isPaga()) {
            throw new IllegalStateException("Parcela já está paga");
        }

        parcela.marcarComoPaga(dataPagamento != null ? dataPagamento : LocalDate.now());
        parcela = parcelaRepository.save(parcela);

        return convertToDTO(parcela);
    }

    public ParcelaDTO cancelarParcela(Integer parcelaId) {
        Parcela parcela = parcelaRepository.findById(parcelaId)
                .orElseThrow(() -> new ObjectNotFoundException("Parcela não encontrada com ID: " + parcelaId));

        if (parcela.isPaga()) {
            throw new IllegalStateException("Não é possível cancelar uma parcela já paga");
        }

        parcela.cancelar();
        parcela = parcelaRepository.save(parcela);

        return convertToDTO(parcela);
    }

    // Query Methods
    public List<ParcelaDTO> listarParcelasPorContaPagar(Integer contaPagarId) {
        return parcelaRepository.findByContaPagarIdOrderByNumeroParcela(contaPagarId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ParcelaDTO> listarParcelasPorContaReceber(Integer contaReceberId) {
        return parcelaRepository.findByContaReceberIdOrderByNumeroParcela(contaReceberId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ParcelaDTO> listarParcelasPorStatus(String status) {
        return parcelaRepository.findByStatusOrderByDataVencimento(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ParcelaDTO> listarParcelasVencidas() {
        return parcelaRepository.findParcelasVencidas(LocalDate.now()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ParcelaDTO> listarParcelasPorPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        return parcelaRepository.findByDataVencimentoBetween(dataInicio, dataFim).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ParcelaDTO> listarProximasParcelasAVencer(Integer dias) {
        LocalDate dataAtual = LocalDate.now();
        LocalDate dataLimite = dataAtual.plusDays(dias);

        return parcelaRepository.findProximasParcelasAVencer(dataAtual, dataLimite).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Utility Methods
    private void validateInstallmentRequest(InstallmentRequestDTO request) {
        if (request == null || !request.isValid()) {
            throw new IllegalArgumentException("Request de parcelamento inválido ou nulo");
        }
        if (request.getNumeroParcelas() > 24) {
            throw new IllegalArgumentException("Número máximo de parcelas é 24");
        }
    }

    private BigDecimal calculateInstallmentAmount(BigDecimal valorTotal, Integer numeroParcelas, Integer parcelaAtual) {
        BigDecimal valorParcela = valorTotal.divide(BigDecimal.valueOf(numeroParcelas), 2, RoundingMode.HALF_UP);

        // Ajusta a última parcela para compensar arredondamentos
        if (parcelaAtual.equals(numeroParcelas)) {
            BigDecimal totalParcelasAnteriores = valorParcela.multiply(BigDecimal.valueOf(numeroParcelas - 1));
            return valorTotal.subtract(totalParcelasAnteriores);
        }
        return valorParcela;
    }

    // Conversion Methods
    private ParcelaDTO convertToDTO(Parcela parcela) {
        ParcelaDTO dto = new ParcelaDTO();
        dto.setId(parcela.getId());
        dto.setNumeroParcela(parcela.getNumeroParcela());
        dto.setTotalParcelas(parcela.getTotalParcelas());
        dto.setValorParcela(parcela.getValorParcela());
        dto.setDataVencimento(parcela.getDataVencimento());
        dto.setDataPagamento(parcela.getDataPagamento());
        dto.setStatus(parcela.getStatus());
        dto.setObservacoes(parcela.getObservacoes());
        dto.setDataCriacao(parcela.getDataCriacao());
        dto.setDataAtualizacao(parcela.getDataAtualizacao());

        if (parcela.getContaPagar() != null) {
            dto.setContaPagarId(parcela.getContaPagar().getId());
            if (parcela.getContaPagar().getCompra() != null &&
                    parcela.getContaPagar().getCompra().getFornecedor() != null) {
                dto.setNomeFornecedor(parcela.getContaPagar().getCompra().getFornecedor().getPessoa().getNome());
            }
        }

        if (parcela.getContaReceber() != null) {
            dto.setContaReceberId(parcela.getContaReceber().getId());
            if (parcela.getContaReceber().getVenda() != null &&
                    parcela.getContaReceber().getVenda().getCliente() != null) {
                dto.setNomeCliente(parcela.getContaReceber().getVenda().getCliente().getPessoa().getNome());
            }
        }

        dto.setVencida(parcela.isVencida());
        dto.setPaga(parcela.isPaga());

        if (parcela.getDataVencimento() != null) {
            dto.setDiasAteVencimento((int) ChronoUnit.DAYS.between(LocalDate.now(), parcela.getDataVencimento()));
        }

        return dto;
    }

    public Parcela salvar(Parcela parcela) {
        return parcelaRepository.save(parcela);
    }
}