package com.joao.osMarmoraria.mapper;

import com.joao.osMarmoraria.domain.ItemOrdemServico;
import com.joao.osMarmoraria.domain.OrdemServico;
import com.joao.osMarmoraria.dtos.ItemOrdemServicoDTO;
import com.joao.osMarmoraria.dtos.OrdemServicoDTO;
import com.joao.osMarmoraria.repository.ItemOrdemServicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Monta {@link OrdemServicoDTO} (com itens) a partir da entidade
 * {@link OrdemServico}. Extraído do {@code OrdemServicoService} para que o
 * serviço cuide do ciclo de vida da OS e não da representação de API.
 */
@Component
@RequiredArgsConstructor
public class OrdemServicoMapper {

    private final ItemOrdemServicoRepository itemOrdemServicoRepository;

    public OrdemServicoDTO toDto(OrdemServico ordemServico) {
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

        List<ItemOrdemServico> itens =
                itemOrdemServicoRepository.findByOrdemServicoIdWithProduto(ordemServico.getId());
        List<ItemOrdemServicoDTO> itensDTO = itens.stream()
                .map(this::itemToDto)
                .collect(Collectors.toList());
        dto.setItens(itensDTO);

        return dto;
    }

    private ItemOrdemServicoDTO itemToDto(ItemOrdemServico item) {
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
