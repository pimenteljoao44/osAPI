package com.joao.osMarmoraria.mapper;

import com.joao.osMarmoraria.domain.Projeto;
import com.joao.osMarmoraria.domain.ProjetoItem;
import com.joao.osMarmoraria.dtos.MedidasProjetoDTO;
import com.joao.osMarmoraria.dtos.ProjetoDTO;
import com.joao.osMarmoraria.dtos.ProjetoItemDTO;
import com.joao.osMarmoraria.repository.ProjetoItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Monta {@link ProjetoDTO} (e seus {@link ProjetoItemDTO}) a partir da
 * entidade {@link Projeto}.
 *
 * <p>Extraído do {@code ProjetoService} (que orquestrava criação/atualização/
 * transição de status do projeto E montava a representação de API na mesma
 * classe — SRP violado). Relocação pura, comportamento preservado: ainda
 * busca os itens com fetch join via {@code findByProjetoIdWithProduto} —
 * continua havendo uma query por chamada a {@code toDto}, catalogado para a
 * Fase 5 (performance) junto dos demais pontos de N+1, fora do escopo desta
 * extração.</p>
 */
@Component
@RequiredArgsConstructor
public class ProjetoMapper {

    private final ProjetoItemRepository projetoItemRepository;

    public ProjetoDTO toDto(Projeto projeto) {
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

        List<ProjetoItem> itens = projetoItemRepository.findByProjetoIdWithProduto(projeto.getId());
        dto.setItens(itens.stream().map(this::itemToDto).collect(Collectors.toList()));

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
