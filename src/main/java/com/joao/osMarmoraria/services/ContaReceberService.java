package com.joao.osMarmoraria.services;

import com.joao.osMarmoraria.domain.*;
import com.joao.osMarmoraria.dtos.ContaReceberDTO;
import com.joao.osMarmoraria.repository.ContaReceberRepository;
import com.joao.osMarmoraria.repository.ProjetoRepository;
import com.joao.osMarmoraria.repository.VendaRepository;
import com.joao.osMarmoraria.services.exceptions.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ContaReceberService {

    @Autowired
    private ContaReceberRepository contaReceberRepository;

    @Autowired
    private VendaRepository vendaRepository;

    @Autowired
    private ProjetoRepository projetoRepository;

    // CRUD Operations
    public List<ContaReceberDTO> listarTodas() {
        return contaReceberRepository.findAllWithDetails().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ContaReceberDTO buscarPorId(Integer id) {
        ContaReceber contaReceber = contaReceberRepository.findByIdWithDetails(id);
        if (contaReceber == null) {
            throw new ObjectNotFoundException("Conta a receber não encontrada com ID: " + id);
        }
        return convertToDTO(contaReceber);
    }

    public ContaReceberDTO criar(ContaReceberDTO contaReceberDTO) {
        ContaReceber contaReceber = fromDTO(contaReceberDTO);
        contaReceber = contaReceberRepository.save(contaReceber);
        return convertToDTO(contaReceber);
    }

    public ContaReceberDTO criarPorVenda(ContaReceberDTO contaReceberDTO) {
        Venda venda = vendaRepository.findById(contaReceberDTO.getVendaId())
                .orElseThrow(() -> new ObjectNotFoundException("Venda não encontrada com ID: " + contaReceberDTO.getVendaId()));

        ContaReceber contaReceber = new ContaReceber();
        contaReceber.setVenda(venda);
        contaReceber.setValor(contaReceberDTO.getValor());
        contaReceber.setDataVencimento(contaReceberDTO.getDataVencimento());
        contaReceber.setStatus(contaReceberDTO.getStatus());
        contaReceber.setDescricao(contaReceberDTO.getObservacoes());
        contaReceber.setDataCriacao(LocalDateTime.now());
        // dataPagamento is null by default for new accounts

        contaReceber = contaReceberRepository.save(contaReceber);
        return convertToDTO(contaReceber);
    }

    public ContaReceberDTO criarPorProjeto(ContaReceberDTO contaReceberDTO) {
        Projeto projeto = projetoRepository.findById(contaReceberDTO.getProjetoId())
                .orElseThrow(() -> new ObjectNotFoundException("Projeto não encontrado com ID: " + contaReceberDTO.getProjetoId()));

        ContaReceber contaReceber = new ContaReceber();
        contaReceber.setProjeto(projeto);
        contaReceber.setValor(contaReceberDTO.getValor());
        contaReceber.setDataVencimento(contaReceberDTO.getDataVencimento());
        contaReceber.setStatus(contaReceberDTO.getStatus());
        contaReceber.setDescricao(contaReceberDTO.getObservacoes());
        contaReceber.setDataCriacao(LocalDateTime.now());
        // dataPagamento is null by default for new accounts

        contaReceber = contaReceberRepository.save(contaReceber);
        return convertToDTO(contaReceber);
    }

    public ContaReceber insert(ContaReceber contaReceber) {
        contaReceber.setId(null);
        return contaReceberRepository.save(contaReceber);
    }

    public ContaReceberDTO atualizar(Integer id, ContaReceberDTO contaReceberDTO) {
        ContaReceber contaReceber = contaReceberRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Conta a receber não encontrada com ID: " + id));

        contaReceber.setValor(contaReceberDTO.getValor());
        contaReceber.setDataVencimento(contaReceberDTO.getDataVencimento());
        contaReceber.setStatus(contaReceberDTO.getStatus());
        contaReceber.setDataPagamento(contaReceberDTO.getDataPagamento()); // Allow null
        contaReceber.setDescricao(contaReceberDTO.getObservacoes());
        contaReceber.setDataAtualizacao(LocalDateTime.now()); // Set dataAtualizacao

        // Handle Venda association update
        if (contaReceberDTO.getVendaId() != null && (contaReceber.getVenda() == null || !contaReceberDTO.getVendaId().equals(contaReceber.getVenda().getVenId()))) {
            Venda venda = vendaRepository.findById(contaReceberDTO.getVendaId())
                    .orElseThrow(() -> new ObjectNotFoundException("Venda não encontrada com ID: " + contaReceberDTO.getVendaId()));
            contaReceber.setVenda(venda);
        } else if (contaReceberDTO.getVendaId() == null) {
            contaReceber.setVenda(null);
        }

        // Handle Projeto association update
        if (contaReceberDTO.getProjetoId() != null && (contaReceber.getProjeto() == null || !contaReceberDTO.getProjetoId().equals(contaReceber.getProjeto().getId()))) {
            Projeto projeto = projetoRepository.findById(contaReceberDTO.getProjetoId())
                    .orElseThrow(() -> new ObjectNotFoundException("Projeto não encontrado com ID: " + contaReceberDTO.getProjetoId()));
            contaReceber.setProjeto(projeto);
        } else if (contaReceberDTO.getProjetoId() == null) {
            contaReceber.setProjeto(null);
        }

        contaReceber = contaReceberRepository.save(contaReceber);
        return convertToDTO(contaReceber);
    }

    public ContaReceberDTO marcarComoRecebida(Integer id, LocalDate dataPagamento) {
        ContaReceber contaReceber = contaReceberRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Conta a receber não encontrada com ID: " + id));

        contaReceber.setStatus("RECEBIDA");
        contaReceber.setDataPagamento(dataPagamento != null ? dataPagamento : LocalDate.now());

        contaReceber = contaReceberRepository.save(contaReceber);
        return convertToDTO(contaReceber);
    }

    public void excluir(Integer id) {
        ContaReceber contaReceber = contaReceberRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Conta a receber não encontrada com ID: " + id));

        if ("RECEBIDA".equals(contaReceber.getStatus())) {
            throw new IllegalStateException("Não é possível excluir uma conta que já foi recebida");
        }

        contaReceberRepository.deleteById(id);
    }

    // Consultas específicas
    public List<ContaReceberDTO> buscarPorStatus(String status) {
        return contaReceberRepository.findByStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ContaReceberDTO> buscarContasReceberPorCliente(Integer clienteId) {
        return contaReceberRepository.findByClienteId(clienteId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ContaReceberDTO> buscarVencidas() {
        LocalDate hoje = LocalDate.now();
        return contaReceberRepository.findContasVencidas(hoje).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ContaReceberDTO> buscarPorPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        return contaReceberRepository.findByPeriodoVencimento(dataInicio, dataFim).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public BigDecimal calcularTotalPendente() {
        BigDecimal total = contaReceberRepository.sumByStatus("PENDENTE");
        return total != null ? total : BigDecimal.ZERO;
    }

    public BigDecimal calcularTotalRecebido() {
        BigDecimal total = contaReceberRepository.sumByStatus("RECEBIDA");
        return total != null ? total : BigDecimal.ZERO;
    }

    private ContaReceber fromDTO(ContaReceberDTO dto) {
        ContaReceber contaReceber = new ContaReceber();
        contaReceber.setValor(dto.getValor());
        contaReceber.setDataVencimento(dto.getDataVencimento()); // @NotNull in DTO, so won't be null
        contaReceber.setDataPagamento(dto.getDataPagamento()); // Can be null, handled by direct assignment
        contaReceber.setStatus(dto.getStatus() != null && !dto.getStatus().isEmpty() ? dto.getStatus() : "PENDENTE");
        contaReceber.setDescricao(dto.getObservacoes());
        contaReceber.setDataCriacao(LocalDateTime.now());
        contaReceber.setParcelado(dto.getParcelado());
        contaReceber.setNumeroParcelas(dto.getNumeroParcelas());

        if (dto.getVendaId() != null) {
            Venda venda = vendaRepository.findById(dto.getVendaId())
                    .orElseThrow(() -> new ObjectNotFoundException("Venda não encontrada com ID: " + dto.getVendaId()));
            contaReceber.setVenda(venda);
        }

        if (dto.getProjetoId() != null) {
            Projeto projeto = projetoRepository.findById(dto.getProjetoId())
                    .orElseThrow(() -> new ObjectNotFoundException("Projeto não encontrado com ID: " + dto.getProjetoId()));
            contaReceber.setProjeto(projeto);
        }

        return contaReceber;
    }

    private ContaReceberDTO convertToDTO(ContaReceber contaReceber) {
        ContaReceberDTO dto = new ContaReceberDTO();
        dto.setObservacoes(contaReceber.getDescricao());
        dto.setId(contaReceber.getId());
        dto.setValor(contaReceber.getValor());
        dto.setDataVencimento(contaReceber.getDataVencimento());
        dto.setDataPagamento(contaReceber.getDataPagamento()); // Can be null, handled by direct assignment
        dto.setStatus(contaReceber.getStatus());
        dto.setDataCriacao(contaReceber.getDataCriacao());
        dto.setParcelado(contaReceber.getParcelado());
        dto.setNumeroParcelas(contaReceber.getNumeroParcelas());
        dto.setDataAtualizacao(contaReceber.getDataAtualizacao()); // Added this line

        if (contaReceber.getVenda() != null) {
            dto.setVendaId(contaReceber.getVenda().getVenId());
            dto.setVenda(contaReceber.getVenda());
        }

        if (contaReceber.getProjeto() != null) {
            dto.setProjetoId(contaReceber.getProjeto().getId());
            dto.setProjeto(contaReceber.getProjeto());
        }

        dto.setDiasAtraso(0);

        if (("PENDENTE".equals(contaReceber.getStatus()) || "VENCIDO".equals(contaReceber.getStatus())) && contaReceber.getDataVencimento() != null) {
            LocalDate dataVencimento = contaReceber.getDataVencimento();
            LocalDate hoje = LocalDate.now();

            long diasDeAtraso = ChronoUnit.DAYS.between(dataVencimento, hoje);
            dto.setDiasAtraso((int) Math.max(0, diasDeAtraso));
        }
        return dto;
    }
}
