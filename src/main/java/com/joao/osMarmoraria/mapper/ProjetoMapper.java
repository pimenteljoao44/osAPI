package com.joao.osMarmoraria.mapper;

import com.joao.osMarmoraria.domain.Projeto;
import com.joao.osMarmoraria.domain.ProjetoItem;
import com.joao.osMarmoraria.dtos.MedidasProjetoDTO;
import com.joao.osMarmoraria.dtos.ProjetoDTO;
import com.joao.osMarmoraria.dtos.ProjetoItemDTO;
import com.joao.osMarmoraria.repository.ProjetoItemRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Monta {@link ProjetoDTO} (e seus {@link ProjetoItemDTO}) a partir da
 * entidade {@link Projeto}.
 *
 * <p>Extraído do {@code ProjetoService} (que orquestrava criação/atualização/
 * transição de status do projeto E montava a representação de API na mesma
 * classe — SRP violado).</p>
 *
 * <p>Performance: {@code toDto} evita a query de itens quando o
 * {@code Projeto} já veio com {@code itens} carregados por fetch join (caso
 * de {@code buscarPorId}/{@code criarProjeto}/{@code atualizarProjeto}, que
 * usam {@code findByIdWithDetails}) — reaproveita a coleção já inicializada
 * em vez de refazer o select. Quando a coleção ainda está lazy (caso das
 * listagens paginadas, que não podem fetch-joinar uma coleção junto com
 * paginação), cai no fallback de uma query por projeto; {@link #toDtoList}
 * resolve esse caso em lote (uma única query {@code IN} para todos os
 * projetos da página) e deve ser preferido no lugar de
 * {@code .map(projetoMapper::toDto)} sempre que a chamada processar uma
 * coleção de projetos.</p>
 */
@Component
@RequiredArgsConstructor
public class ProjetoMapper {

    private final ProjetoItemRepository projetoItemRepository;

    public ProjetoDTO toDto(Projeto projeto) {
        ProjetoDTO dto = montarCamposBasicos(projeto);

        List<ProjetoItem> itens = Hibernate.isInitialized(projeto.getItens())
                ? projeto.getItens()
                : projetoItemRepository.findByProjetoIdWithProduto(projeto.getId());
        dto.setItens(itens.stream().map(this::itemToDto).collect(Collectors.toList()));

        return dto;
    }

    /**
     * Monta os DTOs de uma coleção de projetos buscando os itens de todos
     * eles em uma única query {@code IN}, em vez de uma query por projeto
     * (evita N+1 em listagens paginadas, onde a coleção {@code itens} não
     * pode vir fetch-joinada junto da paginação).
     */
    public List<ProjetoDTO> toDtoList(List<Projeto> projetos) {
        if (projetos.isEmpty()) {
            return List.of();
        }

        List<Integer> projetoIds = projetos.stream().map(Projeto::getId).collect(Collectors.toList());
        Map<Integer, List<ProjetoItem>> itensPorProjeto = projetoItemRepository
                .findByProjetoIdInWithProduto(projetoIds).stream()
                .collect(Collectors.groupingBy(ProjetoItem::getProjetoId));

        return projetos.stream().map(projeto -> {
            ProjetoDTO dto = montarCamposBasicos(projeto);
            List<ProjetoItem> itens = itensPorProjeto.getOrDefault(projeto.getId(), List.of());
            dto.setItens(itens.stream().map(this::itemToDto).collect(Collectors.toList()));
            return dto;
        }).collect(Collectors.toList());
    }

    private ProjetoDTO montarCamposBasicos(Projeto projeto) {
        ProjetoDTO dto = new ProjetoDTO();
        dto.setId(projeto.getId());
        dto.setNome(projeto.getNome());
        dto.setDescricao(projeto.getDescricao());
        dto.setClienteId(projeto.getClienteId());
        dto.setCliente(projeto.getCliente());
        dto.setTipoProjeto(projeto.getTipoProjeto());
        dto.setStatus(projeto.getStatus());
        dto.setDataInicio(projeto.getDataInicio());
        dto.setDataPrevista(projeto.getDataPrevista());
        dto.setDataFinalizacao(projeto.getDataFinalizacao());
        dto.setValorTotal(projeto.getValorTotal());
        dto.setValorMaoObra(projeto.getValorMaoObra());
        dto.setMargemLucro(projeto.getMargemLucro());
        dto.setObservacoes(projeto.getObservacoes());
        dto.setDataCriacao(projeto.getDataCriacao());
        dto.setDataAtualizacao(projeto.getDataAtualizacao());
        dto.setUsuarioCriacao(projeto.getUsuarioCriacao());

        if (projeto.getProfundidade() != null || projeto.getLargura() != null || projeto.getAltura() != null) {
            MedidasProjetoDTO medidas = new MedidasProjetoDTO();
            medidas.setProfundidade(projeto.getProfundidade());
            medidas.setLargura(projeto.getLargura());
            medidas.setAltura(projeto.getAltura());
            medidas.setArea(projeto.getArea());
            medidas.setPerimetro(projeto.getPerimetro());
            medidas.setObservacoes(projeto.getObservacoesMedidas());
            dto.setMedidas(medidas);
        }

        return dto;
    }

    private ProjetoItemDTO itemToDto(ProjetoItem item) {
        ProjetoItemDTO dto = new ProjetoItemDTO();
        dto.setId(item.getId());
        dto.setProjetoId(item.getProjetoId());
        dto.setProdutoId(item.getProdutoId());
        dto.setProduto(item.getProduto());
        dto.setQuantidade(item.getQuantidade());
        dto.setValorUnitario(item.getValorUnitario());
        dto.setValorTotal(item.getValorTotal());
        dto.setObservacoes(item.getObservacoes());
        return dto;
    }
}
