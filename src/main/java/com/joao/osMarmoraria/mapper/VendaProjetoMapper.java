package com.joao.osMarmoraria.mapper;

import com.joao.osMarmoraria.domain.Projeto;
import com.joao.osMarmoraria.domain.Venda;
import com.joao.osMarmoraria.dtos.VendaProjetoDTO;
import org.springframework.stereotype.Component;

import java.time.ZoneId;

/**
 * Converte uma venda de projeto (e o projeto associado) em {@link VendaProjetoDTO}.
 *
 * <p>Extraído do {@code VendaService} (que orquestrava o caso de uso E montava
 * a representação de API na mesma classe — SRP violado). Relocação pura,
 * comportamento preservado: expõe as entidades {@code Cliente}/{@code Projeto}
 * diretamente (sem cópia leve), e {@code podeGerarOS}/{@code podeGerarContaReceber}
 * continuam calculados contra os defaults do DTO (nunca populados a partir da
 * venda real) — pendência de regra de negócio catalogada para revisão futura,
 * fora do escopo desta extração.</p>
 */
@Component
public class VendaProjetoMapper {

    public VendaProjetoDTO toDto(Venda venda, Projeto projeto) {
        VendaProjetoDTO dto = new VendaProjetoDTO();
        dto.setId(venda.getVenId());
        dto.setClienteId(venda.getCliente().getCliId());
        dto.setCliente(venda.getCliente());
        dto.setDataVenda(venda.getDataAbertura().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());

        if (venda.getDataFechamento() != null) {
            dto.setDataEfetivacao(venda.getDataFechamento().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        }

        dto.setValorTotal(venda.getTotal());
        dto.setDesconto(venda.getDesconto());
        dto.setValorFinal(venda.getTotal().subtract(venda.getDesconto()));
        dto.setFormaPagamento(venda.getFormaPagamento().name());
        dto.setStatus(venda.getDataFechamento() != null ? "VENDIDO" : "ORCAMENTO");

        dto.setNomeCliente(venda.getCliente().getPessoa().getNome());

        if (projeto != null) {
            dto.setProjetoId(projeto.getId());
            dto.setProjeto(projeto);
            dto.setNomeProjeto(projeto.getNome());
            dto.setTipoProjeto(projeto.getTipoProjeto().getDescricao());
            dto.setDataPrevistaConclusao(projeto.getDataPrevista());

            dto.setPodeGerarOS("VENDIDO".equals(dto.getStatus()) && !dto.getOrdemServicoGerada());
            dto.setPodeGerarContaReceber("VENDIDO".equals(dto.getStatus()) && !dto.getContaReceberGerada());
        }

        return dto;
    }
}
