package com.joao.osMarmoraria.services;

import com.joao.osMarmoraria.domain.Compra;
import com.joao.osMarmoraria.domain.ContaPagar;
import com.joao.osMarmoraria.dtos.ContaPagarDTO;
import com.joao.osMarmoraria.repository.CompraRepository;
import com.joao.osMarmoraria.repository.ContaPagarRepository;
import com.joao.osMarmoraria.services.exceptions.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ContaPagarService {

    @Autowired
    private ContaPagarRepository contaPagarRepository;

    @Autowired
    private CompraRepository compraRepository;

    // CRUD Operations
    public List<ContaPagarDTO> listarTodas() {
        return contaPagarRepository.findAllWithDetails().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ContaPagarDTO buscarPorId(Integer id) {
        ContaPagar contaPagar = contaPagarRepository.findByIdWithDetails(id);
        if (contaPagar == null) {
            throw new ObjectNotFoundException("Conta a pagar não encontrada com ID: " + id);
        }
        return convertToDTO(contaPagar);
    }

    public List<ContaPagar> buscarPorCompra(Integer compraId) {
        Compra compra = compraRepository.findById(compraId)
                .orElseThrow(() -> new ObjectNotFoundException("Compra não encontrada com ID: " + compraId));

        return contaPagarRepository.findByCompraId(compraId);
    }

    public ContaPagar criar(ContaPagarDTO contaPagarDTO) {
        Compra compra = compraRepository.findById(contaPagarDTO.getCompraId())
                .orElseThrow(() -> new ObjectNotFoundException("Compra não encontrada com ID: " + contaPagarDTO.getCompraId()));

        ContaPagar contaPagar = new ContaPagar();
        contaPagar.setCompra(compra);
        contaPagar.setValor(contaPagarDTO.getValor());
        contaPagar.setDataVencimento(contaPagarDTO.getDataVencimento());
        contaPagar.setStatus(contaPagarDTO.getStatus());

        contaPagar = contaPagarRepository.save(contaPagar);
        return contaPagar;
    }

    public ContaPagar insert(ContaPagar contaPagar) {
        contaPagar.setId(null);
        return contaPagarRepository.save(contaPagar);
    }

    public ContaPagarDTO atualizar(Integer id, ContaPagarDTO contaPagarDTO) {
        ContaPagar contaPagar = contaPagarRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Conta a pagar não encontrada com ID: " + id));

        contaPagar.setValor(contaPagarDTO.getValor());
        contaPagar.setDataVencimento(contaPagarDTO.getDataVencimento());
        contaPagar.setStatus(contaPagarDTO.getStatus());
        contaPagar.setDataPagamento(contaPagarDTO.getDataPagamento());

        contaPagar = contaPagarRepository.save(contaPagar);
        return convertToDTO(contaPagar);
    }

    public ContaPagarDTO marcarComoPaga(Integer id, LocalDate dataPagamento) {
        ContaPagar contaPagar = contaPagarRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Conta a pagar não encontrada com ID: " + id));

        contaPagar.setStatus("PAGA");
        contaPagar.setDataPagamento(dataPagamento != null ? dataPagamento : LocalDate.now());

        contaPagar = contaPagarRepository.save(contaPagar);
        return convertToDTO(contaPagar);
    }

    public void excluir(Integer id) {
        ContaPagar contaPagar = contaPagarRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Conta a pagar não encontrada com ID: " + id));

        if ("PAGA".equals(contaPagar.getStatus())) {
            throw new IllegalStateException("Não é possível excluir uma conta que já foi paga");
        }

        contaPagarRepository.deleteById(id);
    }

    // Consultas específicas
    public List<ContaPagarDTO> buscarPorStatus(String status) {
        return contaPagarRepository.findByStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ContaPagarDTO> buscarVencidas() {
        LocalDate hoje = LocalDate.now();
        return contaPagarRepository.findContasVencidas(hoje).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ContaPagarDTO> buscarPorPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        return contaPagarRepository.findByPeriodoVencimento(dataInicio, dataFim).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public BigDecimal calcularTotalPendente() {
        BigDecimal total = contaPagarRepository.sumByStatus("PENDENTE");
        return total != null ? total : BigDecimal.ZERO;
    }

    public BigDecimal calcularTotalPago() {
        BigDecimal total = contaPagarRepository.sumByStatus("PAGA");
        return total != null ? total : BigDecimal.ZERO;
    }

    // Conversão DTO
    private ContaPagarDTO convertToDTO(ContaPagar contaPagar) {
        ContaPagarDTO dto = new ContaPagarDTO();
        dto.setObservacoes(contaPagar.getObservacoes());
        dto.setId(contaPagar.getId());
        dto.setValor(contaPagar.getValor());
        dto.setDataVencimento(contaPagar.getDataVencimento());
        dto.setDataPagamento(contaPagar.getDataPagamento());
        dto.setStatus(contaPagar.getStatus());

        if (contaPagar.getCompra() != null) {
            dto.setCompraId(contaPagar.getCompra().getComprId());
            dto.setCompra(contaPagar.getCompra());
        }

        dto.setDiasAtraso(0);

        if ("PENDENTE".equals(contaPagar.getStatus()) && contaPagar.getDataVencimento() != null) {
            LocalDate dataVencimento = contaPagar.getDataVencimento();
            LocalDate hoje = LocalDate.now();

            long diasDeAtraso = ChronoUnit.DAYS.between(dataVencimento, hoje);
            dto.setDiasAtraso((int) Math.max(0, diasDeAtraso));
        }
        return dto;
    }
}