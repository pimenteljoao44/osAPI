package com.joao.osMarmoraria.services;

import lombok.RequiredArgsConstructor;

import com.joao.osMarmoraria.domain.*;
import com.joao.osMarmoraria.domain.enums.Status;
import com.joao.osMarmoraria.domain.enums.StatusOrdemServico;
import com.joao.osMarmoraria.domain.enums.StatusProjeto;
import com.joao.osMarmoraria.dtos.ItemOrdemServicoDTO;
import com.joao.osMarmoraria.dtos.OrdemServicoDTO;
import com.joao.osMarmoraria.mapper.OrdemServicoMapper;
import com.joao.osMarmoraria.repository.*;
import com.joao.osMarmoraria.services.exceptions.ObjectNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class OrdemServicoService {

	private final OrdemServicoRepository ordemServicoRepository;

	private final ItemOrdemServicoRepository itemOrdemServicoRepository;

	private final ProjetoRepository projetoRepository;

	private final ProjetoItemRepository projetoItemRepository;

	private final ProjetoService projetoService;

	private final OrdemServicoMapper ordemServicoMapper;

	// CRUD Operations
	public List<OrdemServicoDTO> listarTodas() {
		return ordemServicoRepository.findAll().stream()
				.map(ordemServicoMapper::toDto)
				.collect(Collectors.toList());
	}

	public OrdemServicoDTO buscarPorId(Integer id) {
		OrdemServico ordemServico = ordemServicoRepository.findByIdWithDetails(id)
				.orElseThrow(() -> new ObjectNotFoundException("Ordem de serviço não encontrada com ID: " + id));
		return ordemServicoMapper.toDto(ordemServico);
	}

	public OrdemServicoDTO buscarPorNumero(String numero) {
		OrdemServico ordemServico = ordemServicoRepository.findByNumero(numero)
				.orElseThrow(() -> new ObjectNotFoundException("Ordem de serviço não encontrada com número: " + numero));
		return ordemServicoMapper.toDto(ordemServico);
	}

	public OrdemServicoDTO gerarPorProjeto(Integer projetoId) {
		// Verificar se projeto existe
		Projeto projeto = projetoRepository.findByIdWithDetails(projetoId)
				.orElseThrow(() -> new ObjectNotFoundException("Projeto não encontrado com ID: " + projetoId));

		// Verificar se projeto pode gerar O.S.
		if (!projeto.podeGerarOrdemServico()) {
			throw new IllegalStateException("Projeto deve estar em status 'Orçamento' ou 'Aprovado' para gerar O.S.");
		}

		// Verificar se já existe O.S. para este projeto
		if (ordemServicoRepository.existsByProjetoId(projetoId)) {
			throw new IllegalStateException("Já existe uma ordem de serviço para este projeto");
		}

		// Criar a ordem de serviço
		OrdemServico ordemServico = new OrdemServico();
		ordemServico.setNumero(gerarNumeroOS());
		ordemServico.setProjetoId(projetoId);
		ordemServico.setClienteId(projeto.getClienteId());
		ordemServico.setDataEmissao(LocalDate.now());
		ordemServico.setDataPrevistaConclusao(projeto.getDataPrevista());
		ordemServico.setValorTotal(projeto.getValorTotal());
		ordemServico.setObservacoes("Ordem de serviço gerada automaticamente do projeto: " + projeto.getNome());
		ordemServico.setUsuarioCriacao(projeto.getUsuarioCriacao());

		// Montar instruções técnicas
		StringBuilder instrucoes = new StringBuilder();
		instrucoes.append("PROJETO: ").append(projeto.getNome()).append("\n");
		instrucoes.append("TIPO: ").append(projeto.getTipoProjeto().getDescricao()).append("\n");
		if (projeto.getProfundidade() != null && projeto.getLargura() != null && projeto.getAltura() != null) {
			instrucoes.append("MEDIDAS: ")
					.append(projeto.getProfundidade()).append("m x ")
					.append(projeto.getLargura()).append("m x ")
					.append(projeto.getAltura()).append("m\n");
		}
		if (projeto.getArea() != null) {
			instrucoes.append("ÁREA: ").append(projeto.getArea()).append("m²\n");
		}
		if (projeto.getObservacoes() != null) {
			instrucoes.append("OBSERVAÇÕES: ").append(projeto.getObservacoes()).append("\n");
		}

		ordemServico.setInstrucoesTecnicas(instrucoes.toString());

		ordemServico = ordemServicoRepository.save(ordemServico);

		// Criar itens da O.S. baseados no projeto
		List<ProjetoItem> itensJProjeto = projetoItemRepository.findByProjetoId(projetoId);
		for (ProjetoItem itemProjeto : itensJProjeto) {
			ItemOrdemServico itemOS = new ItemOrdemServico();
			itemOS.setOrdemServicoId(ordemServico.getId());
			itemOS.setProdutoId(itemProjeto.getProdutoId());
			itemOS.setQuantidade(itemProjeto.getQuantidade());
			itemOS.setValorUnitario(itemProjeto.getValorUnitario());
			itemOS.setObservacoes(itemProjeto.getObservacoes());
			itemOrdemServicoRepository.save(itemOS);
		}

		// Atualizar status do projeto para APROVADO
		projetoService.atualizarStatus(projetoId, StatusProjeto.APROVADO);

		return ordemServicoMapper.toDto(ordemServicoRepository.findByIdWithDetails(ordemServico.getId()).get());
	}

	public OrdemServicoDTO atualizarOrdemServico(Integer id, OrdemServicoDTO ordemServicoDTO) {
		OrdemServico ordemExistente = ordemServicoRepository.findById(id)
				.orElseThrow(() -> new ObjectNotFoundException("Ordem de serviço não encontrada com ID: " + id));

		// Atualizar campos editáveis
		ordemExistente.setDataPrevistaInicio(ordemServicoDTO.getDataPrevistaInicio());
		ordemExistente.setDataPrevistaConclusao(ordemServicoDTO.getDataPrevistaConclusao());
		ordemExistente.setResponsavel(ordemServicoDTO.getResponsavel());
		ordemExistente.setObservacoes(ordemServicoDTO.getObservacoes());
		ordemExistente.setInstrucoesTecnicas(ordemServicoDTO.getInstrucoesTecnicas());

		ordemExistente = ordemServicoRepository.save(ordemExistente);
		return ordemServicoMapper.toDto(ordemExistente);
	}

	// Operações de Status
	public OrdemServicoDTO iniciarExecucao(Integer id) {
		OrdemServico ordemServico = ordemServicoRepository.findById(id)
				.orElseThrow(() -> new ObjectNotFoundException("Ordem de serviço não encontrada com ID: " + id));

		ordemServico.iniciarExecucao();
		ordemServico = ordemServicoRepository.save(ordemServico);

		// Atualizar status do projeto relacionado
		if (ordemServico.getProjetoId() != null) {
			projetoService.atualizarStatus(ordemServico.getProjetoId(), StatusProjeto.EM_PRODUCAO);
		}

		return ordemServicoMapper.toDto(ordemServico);
	}

	public OrdemServicoDTO pausarExecucao(Integer id) {
		OrdemServico ordemServico = ordemServicoRepository.findById(id)
				.orElseThrow(() -> new ObjectNotFoundException("Ordem de serviço não encontrada com ID: " + id));

		ordemServico.pausarExecucao();
		ordemServico = ordemServicoRepository.save(ordemServico);
		return ordemServicoMapper.toDto(ordemServico);
	}

	public OrdemServicoDTO retornarExecucao(Integer id) {
		OrdemServico ordemServico = ordemServicoRepository.findById(id)
				.orElseThrow(() -> new ObjectNotFoundException("Ordem de serviço não encontrada com ID: " + id));

		ordemServico.retornarExecucao();
		ordemServico = ordemServicoRepository.save(ordemServico);
		return ordemServicoMapper.toDto(ordemServico);
	}

	public OrdemServicoDTO concluirExecucao(Integer id) {
		OrdemServico ordemServico = ordemServicoRepository.findById(id)
				.orElseThrow(() -> new ObjectNotFoundException("Ordem de serviço não encontrada com ID: " + id));

		ordemServico.concluirExecucao();
		ordemServico = ordemServicoRepository.save(ordemServico);

		// Atualizar status do projeto relacionado
		if (ordemServico.getProjetoId() != null) {
			projetoService.atualizarStatus(ordemServico.getProjetoId(), StatusProjeto.PRONTO);
		}

		return ordemServicoMapper.toDto(ordemServico);
	}

	public OrdemServicoDTO cancelarExecucao(Integer id) {
		OrdemServico ordemServico = ordemServicoRepository.findById(id)
				.orElseThrow(() -> new ObjectNotFoundException("Ordem de serviço não encontrada com ID: " + id));

		ordemServico.cancelarExecucao();
		ordemServico = ordemServicoRepository.save(ordemServico);
		return ordemServicoMapper.toDto(ordemServico);
	}

	// Consultas específicas
	public List<OrdemServicoDTO> buscarPorStatus(StatusOrdemServico status) {
		return ordemServicoRepository.findByStatus(status).stream()
				.map(ordemServicoMapper::toDto)
				.collect(Collectors.toList());
	}

	public List<OrdemServicoDTO> buscarPorCliente(Integer clienteId) {
		return ordemServicoRepository.findByClienteId(clienteId).stream()
				.map(ordemServicoMapper::toDto)
				.collect(Collectors.toList());
	}

	public List<OrdemServicoDTO> buscarPorPeriodo(LocalDate dataInicio, LocalDate dataFim) {
		return ordemServicoRepository.findByPeriodo(dataInicio, dataFim).stream()
				.map(ordemServicoMapper::toDto)
				.collect(Collectors.toList());
	}

	// Métodos auxiliares
	private String gerarNumeroOS() {
		String numero;
		do {
			numero = "OS" + System.currentTimeMillis();
		} while (ordemServicoRepository.existsByNumero(numero));
		return numero;
	}

}
