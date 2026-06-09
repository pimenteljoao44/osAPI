package com.joao.osMarmoraria.mapper;

import com.joao.osMarmoraria.domain.Produto;
import com.joao.osMarmoraria.domain.Projeto;
import com.joao.osMarmoraria.domain.ProjetoItem;
import com.joao.osMarmoraria.dtos.PecaDTO;
import com.joao.osMarmoraria.dtos.ProjetoDTO;
import com.joao.osMarmoraria.dtos.ProjetoItemDTO;
import com.joao.osMarmoraria.dtos.RecorteDTO;
import com.joao.osMarmoraria.repository.ProdutoRepository;
import com.joao.osMarmoraria.repository.ProjetoItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Monta {@link ProjetoDTO} a partir da entidade {@link Projeto}, incluindo
 * peças, recortes e itens. Extraído do {@code ProjetoService} para isolar a
 * montagem do DTO (que depende de consultas a itens e produtos) do fluxo de
 * negócio.
 *
 * <p>Nota de performance: {@code itemToDto} ainda re-busca o produto por id,
 * mesmo com o fetch join de {@code findByProjetoIdWithProduto} — N+1
 * catalogado para correção na Fase 5 do REFACTORING.md.</p>
 */
@Component
@RequiredArgsConstructor
public class ProjetoMapper {

    private final ProjetoItemRepository projetoItemRepository;
    private final ProdutoRepository produtoRepository;

    public ProjetoDTO toDto(Projeto projeto) {
        if (projeto == null) {
            return null;
        }

        ProjetoDTO dto = new ProjetoDTO();
        dto.setId(projeto.getId());
        dto.setNome(projeto.getNome());
        dto.setDescricao(projeto.getDescricao());
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

        if (projeto.getUsuarioCriacao() != null) {
            dto.setUsuarioCriacao(projeto.getUsuarioCriacao().getId());
        } else {
            throw new IllegalStateException("Projeto não possui usuário de criação associado");
        }

        if (projeto.getCliente() != null) {
            dto.setClienteId(projeto.getCliente().getCliId());
            dto.setClienteNome(projeto.getCliente().getPessoa().getNome());
        }

        if (projeto.getPecas() != null) {
            dto.setPecas(projeto.getPecas().stream().map(peca -> {
                PecaDTO pecaDTO = new PecaDTO();
                pecaDTO.setId(peca.getId());
                pecaDTO.setNome(peca.getNome());
                pecaDTO.setTipo(peca.getTipo());
                pecaDTO.setLargura(peca.getLargura());
                pecaDTO.setAltura(peca.getAltura());
                pecaDTO.setEspessura(peca.getEspessura());
                pecaDTO.setUnidade(peca.getUnidade());
                pecaDTO.setX(peca.getX());
                pecaDTO.setY(peca.getY());
                if (peca.getRecortes() != null) {
                    pecaDTO.setRecortes(peca.getRecortes().stream().map(recorte -> {
                        RecorteDTO recorteDTO = new RecorteDTO();
                        recorteDTO.setTipo(recorte.getTipo());
                        recorteDTO.setLargura(recorte.getLargura());
                        recorteDTO.setAltura(recorte.getAltura());
                        recorteDTO.setPosicaoX(recorte.getPosicaoX());
                        recorteDTO.setPosicaoY(recorte.getPosicaoY());
                        return recorteDTO;
                    }).collect(Collectors.toList()));
                }
                return pecaDTO;
            }).collect(Collectors.toList()));
        }

        List<ProjetoItem> itens = projetoItemRepository.findByProjetoIdWithProduto(projeto.getId());
        List<ProjetoItemDTO> itensDTO = itens.stream()
                .map(this::itemToDto)
                .collect(Collectors.toList());
        dto.setItens(itensDTO);

        return dto;
    }

    private ProjetoItemDTO itemToDto(ProjetoItem item) {
        Integer produtoId = item.getProdutoId();
        if (produtoId == null) {
            throw new IllegalArgumentException("ID do Produto não pode ser nulo para o item de projeto.");
        }

        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado para o ID: " + produtoId));

        ProjetoItemDTO dto = new ProjetoItemDTO();
        dto.setId(item.getId());
        dto.setProjetoId(item.getProjetoId());
        dto.setProdutoId(produto.getProdId());
        dto.setProdutoNome(produto.getNome());
        dto.setQuantidade(item.getQuantidade());
        dto.setValorUnitario(item.getValorUnitario());
        dto.setValorTotal(item.getValorTotal());
        dto.setObservacoes(item.getObservacoes());
        return dto;
    }
}
