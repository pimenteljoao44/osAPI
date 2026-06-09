package com.joao.osMarmoraria.mapper;

import com.joao.osMarmoraria.domain.Cliente;
import com.joao.osMarmoraria.domain.Produto;
import com.joao.osMarmoraria.domain.Projeto;
import com.joao.osMarmoraria.domain.ProjetoItem;
import com.joao.osMarmoraria.domain.Venda;
import com.joao.osMarmoraria.dtos.VendaProjetoDTO;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Converte uma venda de projeto (e o projeto associado) em {@link VendaProjetoDTO}.
 *
 * <p>O mapeamento é escrito à mão (em vez de MapStruct) porque monta cópias
 * "leves" de {@code Projeto} e {@code Cliente} — apenas os campos que a API
 * expõe — para limitar a profundidade serializada e evitar
 * {@code LazyInitializationException}. Extraído do {@code VendaService} para
 * que o serviço orquestre o caso de uso e não monte representação de API (SRP).</p>
 */
@Component
public class VendaProjetoMapper {

    public VendaProjetoDTO toDto(Venda venda, Projeto projeto) {
        VendaProjetoDTO dto = new VendaProjetoDTO();
        dto.setId(venda.getVenId());
        dto.setClienteId(venda.getCliente().getCliId());
        dto.setDataVenda(venda.getDataAbertura().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());

        if (venda.getDataFechamento() != null) {
            dto.setDataEfetivacao(venda.getDataFechamento().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        }

        dto.setValorTotal(venda.getTotal());
        dto.setDesconto(venda.getDesconto());
        dto.setValorFinal(venda.getTotal().subtract(venda.getDesconto()));
        dto.setFormaPagamento(venda.getFormaPagamento().name());
        dto.setNumeroParcelas(venda.getNumeroParcelas());
        dto.setObservacoes(venda.getObservacoes());
        dto.setStatus(venda.getDataFechamento() != null ? "VENDIDO" : "ORCAMENTO");

        if (projeto != null) {
            dto.setProjetoId(projeto.getId());
            dto.setNomeProjeto(projeto.getNome());
            dto.setTipoProjeto(projeto.getTipoProjeto().getDescricao());
            dto.setDataPrevistaConclusao(projeto.getDataPrevista());
            dto.setPodeGerarOS(true);
            dto.setPodeGerarContaReceber(true);
            dto.setProjeto(copiaLeveDoProjeto(projeto));
        }

        Cliente clienteLeve = new Cliente();
        clienteLeve.setCliId(venda.getCliente().getCliId());
        clienteLeve.setPessoa(venda.getCliente().getPessoa());
        dto.setCliente(clienteLeve);
        dto.setNomeCliente(venda.getCliente().getPessoa().getNome());

        return dto;
    }

    private Projeto copiaLeveDoProjeto(Projeto projeto) {
        List<ProjetoItem> itens = new ArrayList<>();
        if (projeto.getItens() != null) {
            for (ProjetoItem item : projeto.getItens()) {
                itens.add(copiaLeveDoItem(item));
            }
        }

        Projeto projetoLeve = new Projeto();
        projetoLeve.setId(projeto.getId());
        projetoLeve.setNome(projeto.getNome());
        projetoLeve.setDescricao(projeto.getDescricao());
        projetoLeve.setDataPrevista(projeto.getDataPrevista());
        projetoLeve.setItens(itens);
        return projetoLeve;
    }

    private ProjetoItem copiaLeveDoItem(ProjetoItem item) {
        ProjetoItem itemCopia = new ProjetoItem();
        itemCopia.setId(item.getId());
        itemCopia.setQuantidade(item.getQuantidade());
        itemCopia.setValorUnitario(item.getValorUnitario());

        if (item.getProduto() != null) {
            Produto produtoCopia = new Produto();
            produtoCopia.setProdId(item.getProduto().getProdId());
            produtoCopia.setNome(item.getProduto().getNome());
            itemCopia.setProduto(produtoCopia);
        }
        return itemCopia;
    }
}
