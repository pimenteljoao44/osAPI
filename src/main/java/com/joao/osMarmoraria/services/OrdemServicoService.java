package com.joao.osMarmoraria.services;

import com.joao.osMarmoraria.domain.*;
import com.joao.osMarmoraria.domain.enums.Status;
import com.joao.osMarmoraria.domain.enums.StatusOrdemServico;
import com.joao.osMarmoraria.domain.enums.StatusProjeto;
import com.joao.osMarmoraria.dtos.ItemOrdemServicoDTO;
import com.joao.osMarmoraria.dtos.OrdemServicoDTO;
import com.joao.osMarmoraria.dtos.AgendamentoDTO;
import com.joao.osMarmoraria.repository.*;
import com.joao.osMarmoraria.services.exceptions.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
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
public class OrdemServicoService {

	@Autowired
	private OrdemServicoRepository ordemServicoRepository;

	@Autowired
	private ItemOrdemServicoRepository itemOrdemServicoRepository;

	@Autowired
	private ProjetoRepository projetoRepository;

	@Autowired
	private ProjetoItemRepository projetoItemRepository;

	@Autowired
	private ProjetoService projetoService;

	// CRUD Operations
	public List<OrdemServicoDTO> listarTodas() {
		return ordemServicoRepository.findAll().stream()
				.map(this::convertToDTO)
				.collect(Collectors.toList());
	}

	public OrdemServicoDTO buscarPorId(Integer id) {
		OrdemServico ordemServico = ordemServicoRepository.findByIdWithDetails(id)
				.orElseThrow(() -> new ObjectNotFoundException("Ordem de serviço não encontrada com ID: " + id));
		return convertToDTO(ordemServico);
	}

	public OrdemServicoDTO buscarPorNumero(String numero) {
		OrdemServico ordemServico = ordemServicoRepository.findByNumero(numero)
				.orElseThrow(() -> new ObjectNotFoundException("Ordem de serviço não encontrada com número: " + numero));
		return convertToDTO(ordemServico);
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
		ordemServico.setClienteId(projeto.getCliente().getCliId());
		ordemServico.setDataEmissao(LocalDate.now());
		ordemServico.setDataPrevistaConclusao(projeto.getDataPrevista());
		ordemServico.setValorTotal(projeto.getValorTotal());
		ordemServico.setObservacoes("Ordem de serviço gerada automaticamente do projeto: " + projeto.getNome());
		ordemServico.setUsuarioCriacao(projeto.getUsuarioCriacao().getId());

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

		return convertToDTO(ordemServicoRepository.findByIdWithDetails(ordemServico.getId()).get());
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
		return convertToDTO(ordemExistente);
	}

	// Operações de Aprovação e Agendamento
	public OrdemServicoDTO aprovarOrdemServico(Integer id, String observacoes) {
		OrdemServico ordemServico = ordemServicoRepository.findById(id)
				.orElseThrow(() -> new ObjectNotFoundException("Ordem de serviço não encontrada com ID: " + id));

		if (!ordemServico.podeSerAprovada()) {
			throw new IllegalStateException("Ordem de serviço deve estar pendente para ser aprovada");
		}

		ordemServico.aprovar();

		// Adicionar observações da aprovação
		if (observacoes != null && !observacoes.trim().isEmpty()) {
			String observacoesAtuais = ordemServico.getObservacoes() != null ? ordemServico.getObservacoes() : "";
			ordemServico.setObservacoes(observacoesAtuais + "\n[APROVAÇÃO] " + observacoes);
		}

		ordemServico = ordemServicoRepository.save(ordemServico);

		// Atualizar status do projeto relacionado
		if (ordemServico.getProjetoId() != null) {
			projetoService.atualizarStatus(ordemServico.getProjetoId(), StatusProjeto.APROVADO);
		}

		return convertToDTO(ordemServico);
	}

	public OrdemServicoDTO agendarOrdemServico(Integer id, AgendamentoDTO agendamentoDTO) {
		OrdemServico ordemServico = ordemServicoRepository.findById(id)
				.orElseThrow(() -> new ObjectNotFoundException("Ordem de serviço não encontrada com ID: " + id));

		if (!ordemServico.podeSerAgendada()) {
			throw new IllegalStateException("Ordem de serviço deve estar aprovada ou pendente para ser agendada");
		}

		// Validar datas
		LocalDate dataInicio = agendamentoDTO.getDataPrevistaInicio();
		LocalDate dataConclusao = agendamentoDTO.getDataPrevistaConclusao();

		if (dataInicio == null) {
			throw new IllegalArgumentException("Data prevista de início é obrigatória");
		}

		if (dataInicio.isBefore(LocalDate.now())) {
			throw new IllegalArgumentException("Data prevista de início não pode ser anterior à data atual");
		}

		if (dataConclusao != null && dataConclusao.isBefore(dataInicio)) {
			throw new IllegalArgumentException("Data prevista de conclusão não pode ser anterior à data de início");
		}

		// Agendar a ordem de serviço
		ordemServico.agendar(dataInicio, dataConclusao);

		// Definir responsável se informado
		if (agendamentoDTO.getResponsavel() != null && !agendamentoDTO.getResponsavel().trim().isEmpty()) {
			ordemServico.setResponsavel(agendamentoDTO.getResponsavel());
		}

		// Adicionar observações do agendamento
		if (agendamentoDTO.getObservacoes() != null && !agendamentoDTO.getObservacoes().trim().isEmpty()) {
			String observacoesAtuais = ordemServico.getObservacoes() != null ? ordemServico.getObservacoes() : "";
			ordemServico.setObservacoes(observacoesAtuais + "\n[AGENDAMENTO] " + agendamentoDTO.getObservacoes());
		}

		ordemServico = ordemServicoRepository.save(ordemServico);
		return convertToDTO(ordemServico);
	}

	public OrdemServicoDTO aprovarEAgendar(Integer id, AgendamentoDTO agendamentoDTO) {
		OrdemServico ordemServico = ordemServicoRepository.findById(id)
				.orElseThrow(() -> new ObjectNotFoundException("Ordem de serviço não encontrada com ID: " + id));

		if (ordemServico.getStatus() != StatusOrdemServico.PENDENTE) {
			throw new IllegalStateException("Ordem de serviço deve estar pendente para ser aprovada e agendada");
		}

		// Primeiro aprovar
		aprovarOrdemServico(id, "Aprovada e agendada automaticamente");

		// Depois agendar
		return agendarOrdemServico(id, agendamentoDTO);
	}

	// Operações de Status Existentes
	public OrdemServicoDTO iniciarExecucao(Integer id) {
		OrdemServico ordemServico = ordemServicoRepository.findById(id)
				.orElseThrow(() -> new ObjectNotFoundException("Ordem de serviço não encontrada com ID: " + id));

		ordemServico.iniciarExecucao();
		ordemServico = ordemServicoRepository.save(ordemServico);

		// Atualizar status do projeto relacionado
		if (ordemServico.getProjetoId() != null) {
			projetoService.atualizarStatus(ordemServico.getProjetoId(), StatusProjeto.EM_PRODUCAO);
		}

		return convertToDTO(ordemServico);
	}

	public OrdemServicoDTO pausarExecucao(Integer id) {
		OrdemServico ordemServico = ordemServicoRepository.findById(id)
				.orElseThrow(() -> new ObjectNotFoundException("Ordem de serviço não encontrada com ID: " + id));

		ordemServico.pausarExecucao();
		ordemServico = ordemServicoRepository.save(ordemServico);
		return convertToDTO(ordemServico);
	}

	public OrdemServicoDTO retornarExecucao(Integer id) {
		OrdemServico ordemServico = ordemServicoRepository.findById(id)
				.orElseThrow(() -> new ObjectNotFoundException("Ordem de serviço não encontrada com ID: " + id));

		ordemServico.retornarExecucao();
		ordemServico = ordemServicoRepository.save(ordemServico);
		return convertToDTO(ordemServico);
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

		return convertToDTO(ordemServico);
	}

	public OrdemServicoDTO cancelarExecucao(Integer id) {
		OrdemServico ordemServico = ordemServicoRepository.findById(id)
				.orElseThrow(() -> new ObjectNotFoundException("Ordem de serviço não encontrada com ID: " + id));

		ordemServico.cancelarExecucao();
		ordemServico = ordemServicoRepository.save(ordemServico);
		return convertToDTO(ordemServico);
	}

	// Consultas específicas
	public List<OrdemServicoDTO> buscarPorStatus(StatusOrdemServico status) {
		return ordemServicoRepository.findByStatus(status).stream()
				.map(this::convertToDTO)
				.collect(Collectors.toList());
	}

	public List<OrdemServicoDTO> buscarPorCliente(Integer clienteId) {
		return ordemServicoRepository.findByClienteId(clienteId).stream()
				.map(this::convertToDTO)
				.collect(Collectors.toList());
	}

	public List<OrdemServicoDTO> buscarPorPeriodo(LocalDate dataInicio, LocalDate dataFim) {
		return ordemServicoRepository.findByPeriodo(dataInicio, dataFim).stream()
				.map(this::convertToDTO)
				.collect(Collectors.toList());
	}

	public List<OrdemServicoDTO> buscarPendentesAprovacao() {
		return buscarPorStatus(StatusOrdemServico.PENDENTE);
	}

	public List<OrdemServicoDTO> buscarAprovadasSemAgendamento() {
		return ordemServicoRepository.findByStatusAndDataPrevistaInicioIsNull(StatusOrdemServico.APROVADA).stream()
				.map(this::convertToDTO)
				.collect(Collectors.toList());
	}

	public List<OrdemServicoDTO> buscarAgendadasParaHoje() {
		return ordemServicoRepository.findByStatusAndDataPrevistaInicio(StatusOrdemServico.AGENDADA, LocalDate.now()).stream()
				.map(this::convertToDTO)
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

	// Conversão DTO
	private OrdemServicoDTO convertToDTO(OrdemServico ordemServico) {
		OrdemServicoDTO dto = new OrdemServicoDTO();
		dto.setId(ordemServico.getId());
		dto.setNumero(ordemServico.getNumero());
		dto.setProjetoId(ordemServico.getProjetoId());
		dto.setProjeto(ordemServico.getProjeto());
		dto.setClienteId(ordemServico.getClienteId());
		dto.setCliente(ordemServico.getCliente());
		dto.setDataEmissao(ordemServico.getDataEmissao());
		dto.setDataPrevistaInicio(ordemServico.getDataPrevistaInicio());
		dto.setDataPrevistaConclusao(ordemServico.getDataPrevistaConclusao());
		dto.setDataInicio(ordemServico.getDataInicio());
		dto.setDataConclusao(ordemServico.getDataConclusao());
		dto.setStatus(ordemServico.getStatus());
		dto.setResponsavel(ordemServico.getResponsavel());
		dto.setObservacoes(ordemServico.getObservacoes());
		dto.setInstrucoesTecnicas(ordemServico.getInstrucoesTecnicas());
		dto.setValorTotal(ordemServico.getValorTotal());
		dto.setDataCriacao(ordemServico.getDataCriacao());
		dto.setDataAtualizacao(ordemServico.getDataAtualizacao());
		dto.setUsuarioCriacao(ordemServico.getUsuarioCriacao());

		// Itens
		List<ItemOrdemServico> itens = itemOrdemServicoRepository.findByOrdemServicoIdWithProduto(ordemServico.getId());
		List<ItemOrdemServicoDTO> itensDTO = itens.stream().map(this::convertItemToDTO).collect(Collectors.toList());
		dto.setItens(itensDTO);

		return dto;
	}

	private ItemOrdemServicoDTO convertItemToDTO(ItemOrdemServico item) {
		ItemOrdemServicoDTO dto = new ItemOrdemServicoDTO();
		dto.setId(item.getId());
		dto.setOrdemServicoId(item.getOrdemServicoId());
		dto.setProdutoId(item.getProdutoId());
		dto.setProduto(item.getProduto());
		dto.setQuantidade(item.getQuantidade());
		dto.setValorUnitario(item.getValorUnitario());
		dto.setValorTotal(item.getValorTotal());
		dto.setObservacoes(item.getObservacoes());
		return dto;
	}
}