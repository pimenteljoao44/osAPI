package com.joao.osMarmoraria.services;

import lombok.RequiredArgsConstructor;
import com.joao.osMarmoraria.domain.Produto;
import com.joao.osMarmoraria.domain.Projeto;
import com.joao.osMarmoraria.domain.ProjetoItem;
import com.joao.osMarmoraria.domain.enums.StatusProjeto;
import com.joao.osMarmoraria.domain.enums.TipoProjeto;
import com.joao.osMarmoraria.dtos.*;
import com.joao.osMarmoraria.mapper.ProjetoMapper;
import com.joao.osMarmoraria.repository.ProdutoRepository;
import com.joao.osMarmoraria.repository.ProjetoItemRepository;
import com.joao.osMarmoraria.repository.ProjetoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ProjetoService {

    private final ProjetoRepository projetoRepository;

    private final ProjetoItemRepository projetoItemRepository;

    private final ProdutoRepository produtoRepository;

    private final ProjetoMapper projetoMapper;

    // CRUD Operations
    @Transactional(readOnly = true)
    public Page<ProjetoDTO> listarProjetos(Pageable pageable) {
        Page<Projeto> page = projetoRepository.findAllWithCliente(pageable);
        return new PageImpl<>(projetoMapper.toDtoList(page.getContent()), pageable, page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Page<ProjetoDTO> listarComFiltros(String nome, StatusProjeto status, TipoProjeto tipoProjeto, Integer clienteId, Pageable pageable) {
        Page<Projeto> page = projetoRepository.findWithFilters(nome, status, tipoProjeto, clienteId, pageable);
        return new PageImpl<>(projetoMapper.toDtoList(page.getContent()), pageable, page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public ProjetoDTO buscarPorId(Integer id) {
        Projeto projeto = projetoRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new EntityNotFoundException("Projeto não encontrado com ID: " + id));
        return projetoMapper.toDto(projeto);
    }

    @Transactional
    public ProjetoDTO criarProjeto(ProjetoDTO projetoDTO) {
        if (projetoRepository.existsByNomeAndClienteId(projetoDTO.getNome(), projetoDTO.getClienteId())) {
            throw new IllegalArgumentException("Já existe um projeto com este nome para o cliente informado");
        }

        Projeto projeto = convertToEntity(projetoDTO);

        calcularMedidasProjeto(projeto);

        projeto.setValorTotal(BigDecimal.ONE); // Valor temporário para passar validação
        projeto = projetoRepository.save(projeto);

        if (projetoDTO.getItens() != null && !projetoDTO.getItens().isEmpty()) {
            salvarItens(projeto.getId(), projetoDTO.getItens());
        }

        calcularValoresItens(projeto);
        projeto.recalcularValorTotal();

        projeto = projetoRepository.save(projeto);

        return projetoMapper.toDto(projetoRepository.findByIdWithDetails(projeto.getId())
                .orElseThrow(() -> new RuntimeException("Projeto não encontrado após criação")));
    }

    public ProjetoDTO atualizarProjeto(Integer id, ProjetoDTO projetoDTO) {
        Projeto projetoExistente = projetoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Projeto não encontrado com ID: " + id));

        // Atualizar campos
        projetoExistente.setNome(projetoDTO.getNome());
        projetoExistente.setDescricao(projetoDTO.getDescricao());
        projetoExistente.setTipoProjeto(projetoDTO.getTipoProjeto());
        projetoExistente.setDataPrevista(projetoDTO.getDataPrevista());
        projetoExistente.setObservacoes(projetoDTO.getObservacoes());
        projetoExistente.setMargemLucro(projetoDTO.getMargemLucro());

        // Atualizar medidas
        if (projetoDTO.getMedidas() != null) {
            projetoExistente.setProfundidade(projetoDTO.getMedidas().getProfundidade());
            projetoExistente.setLargura(projetoDTO.getMedidas().getLargura());
            projetoExistente.setAltura(projetoDTO.getMedidas().getAltura());
            projetoExistente.setObservacoesMedidas(projetoDTO.getMedidas().getObservacoes());
        }

        // Recalcular valores
        calcularMedidasProjeto(projetoExistente);

        // Atualizar itens
        projetoItemRepository.deleteByProjetoId(id);
        if (projetoDTO.getItens() != null && !projetoDTO.getItens().isEmpty()) {
            salvarItens(id, projetoDTO.getItens());
        }

        calcularValoresItens(projetoExistente);
        projetoExistente.recalcularValorTotal();

        projetoExistente = projetoRepository.save(projetoExistente);
        return projetoMapper.toDto(projetoRepository.findByIdWithDetails(projetoExistente.getId()).get());
    }

    public void excluirProjeto(Integer id) {
        Projeto projeto = projetoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Projeto não encontrado com ID: " + id));

        if (projeto.getStatus() == StatusProjeto.EM_PRODUCAO || projeto.getStatus() == StatusProjeto.ENTREGUE) {
            throw new IllegalStateException("Não é possível excluir projeto em produção ou entregue");
        }

        projetoRepository.delete(projeto);
    }

    // Operações de Status
    public ProjetoDTO atualizarStatus(Integer id, StatusProjeto novoStatus) {
        Projeto projeto = projetoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Projeto não encontrado com ID: " + id));

        StatusProjeto statusAtual = projeto.getStatus();

        // Validar transição de status
        if (!isTransicaoStatusValida(statusAtual, novoStatus)) {
            throw new IllegalStateException("Transição de status inválida: " + statusAtual + " -> " + novoStatus);
        }

        projeto.setStatus(novoStatus);

        // Definir datas baseadas no status
        switch (novoStatus) {
            case APROVADO:
                projeto.setDataInicio(LocalDate.now());
                break;
            case ENTREGUE:
                projeto.setDataFinalizacao(LocalDate.now());
                break;
        }

        projeto = projetoRepository.save(projeto);
        return projetoMapper.toDto(projeto);
    }

    // Cálculo de Orçamento
    @Transactional(readOnly = true)
    public CalculoOrcamentoDTO calcularOrcamento(ProjetoDTO projetoDTO) {
        CalculoOrcamentoDTO calculo = new CalculoOrcamentoDTO();

        // Calcular valor dos materiais
        BigDecimal valorMateriais = BigDecimal.ZERO;
        if (projetoDTO.getItens() != null) {
            valorMateriais = projetoDTO.getItens().stream()
                    .map(item -> item.getQuantidade().multiply(item.getValorUnitario()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        // Calcular mão de obra baseada na área (R$ 50 por m²)
        BigDecimal valorMaoObra = BigDecimal.ZERO;
        if (projetoDTO.getMedidas() != null &&
                projetoDTO.getMedidas().getProfundidade() != null &&
                projetoDTO.getMedidas().getLargura() != null) {

            BigDecimal area = projetoDTO.getMedidas().getProfundidade()
                    .multiply(projetoDTO.getMedidas().getLargura());
            valorMaoObra = area.multiply(new BigDecimal("50.00"));
        }

        // Calcular valor total com margem de lucro
        BigDecimal subtotal = valorMateriais.add(valorMaoObra);
        BigDecimal margemLucro = projetoDTO.getMargemLucro() != null ?
                projetoDTO.getMargemLucro() : new BigDecimal("20.00");
        BigDecimal multiplicadorLucro = BigDecimal.ONE.add(margemLucro.divide(new BigDecimal("100")));
        BigDecimal valorTotal = subtotal.multiply(multiplicadorLucro);

        calculo.setValorMateriais(valorMateriais);
        calculo.setValorMaoObra(valorMaoObra);
        calculo.setMargemLucro(margemLucro);
        calculo.setValorTotal(valorTotal);

        return calculo;
    }

    // Materiais Sugeridos
    @Transactional(readOnly = true)
    public List<MaterialSugeridoDTO> obterMateriaisSugeridos(TipoProjeto tipoProjeto, MedidasProjetoDTO medidas) {
        List<MaterialSugeridoDTO> materiais = new ArrayList<>();

        // Lógica para sugerir materiais baseado no tipo de projeto
        List<Integer> produtoIds = obterProdutosSugeridosPorTipo(tipoProjeto);

        for (Integer produtoId : produtoIds) {
            produtoRepository.findById(produtoId).ifPresent(produto -> {
                MaterialSugeridoDTO material = new MaterialSugeridoDTO();
                material.setProdutoId(produto.getProdId());
                material.setProduto(produto);
                material.setQuantidadeRecomendada(calcularQuantidadeRecomendada(produto, medidas, tipoProjeto));
                material.setAplicacao(obterAplicacaoProduto(produto, tipoProjeto));
                materiais.add(material);
            });
        }

        return materiais;
    }

    // Relatórios
    @Transactional(readOnly = true)
    public List<ProjetoDTO> obterProjetosPorPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        List<Projeto> projetos = projetoRepository.findByPeriodo(dataInicio, dataFim);
        return projetoMapper.toDtoList(projetos);
    }

    // Métodos auxiliares privados
    private void salvarItens(Integer projetoId, List<ProjetoItemDTO> itensDTO) {
        for (ProjetoItemDTO itemDTO : itensDTO) {
            ProjetoItem item = new ProjetoItem();
            item.setProjetoId(projetoId);
            item.setProdutoId(itemDTO.getProdutoId());
            item.setQuantidade(itemDTO.getQuantidade());
            item.setValorUnitario(itemDTO.getValorUnitario());
            item.setObservacoes(itemDTO.getObservacoes());
            projetoItemRepository.save(item);
        }
    }

    private void calcularMedidasProjeto(Projeto projeto) {
        if (projeto.getProfundidade() != null && projeto.getLargura() != null) {
            projeto.setArea(projeto.getProfundidade().multiply(projeto.getLargura()));
            projeto.setPerimetro(projeto.getProfundidade().add(projeto.getLargura()).multiply(new BigDecimal("2")));
        }
    }

    private void calcularValoresItens(Projeto projeto) {
        List<ProjetoItem> itens = projetoItemRepository.findByProjetoId(projeto.getId());
        BigDecimal valorMateriais = itens.stream()
                .map(ProjetoItem::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calcular mão de obra baseada na área
        BigDecimal valorMaoObra = BigDecimal.ZERO;
        if (projeto.getArea() != null) {
            valorMaoObra = projeto.getArea().multiply(new BigDecimal("50.00"));
        }

        projeto.setValorMaoObra(valorMaoObra);
    }

    private boolean isTransicaoStatusValida(StatusProjeto statusAtual, StatusProjeto novoStatus) {
        // Definir transições válidas
        switch (statusAtual) {
            case ORCAMENTO:
                return novoStatus == StatusProjeto.APROVADO || novoStatus == StatusProjeto.CANCELADO;
            case APROVADO:
                return novoStatus == StatusProjeto.EM_PRODUCAO || novoStatus == StatusProjeto.CANCELADO;
            case EM_PRODUCAO:
                return novoStatus == StatusProjeto.PRONTO || novoStatus == StatusProjeto.CANCELADO;
            case PRONTO:
                return novoStatus == StatusProjeto.ENTREGUE;
            case ENTREGUE:
            case CANCELADO:
                return false; // Estados finais
            default:
                return false;
        }
    }

    private List<Integer> obterProdutosSugeridosPorTipo(TipoProjeto tipoProjeto) {
        // Retornar IDs de produtos comumente usados por tipo de projeto
        switch (tipoProjeto) {
            case BANHEIRO:
                return Arrays.asList(1, 2, 3);
            case COZINHA:
                return Arrays.asList(4, 5, 6);
            case CUBA:
                return Arrays.asList(7, 8, 9);
            default:
                return Arrays.asList(1, 2, 3);
        }
    }


    private BigDecimal calcularQuantidadeRecomendada(Produto produto, MedidasProjetoDTO medidas, TipoProjeto tipoProjeto) {
        // Lógica simples para calcular quantidade baseada na área
        if (medidas.getProfundidade() != null && medidas.getLargura() != null) {
            BigDecimal area = medidas.getProfundidade().multiply(medidas.getLargura());
            return area.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ONE;
    }

    private String obterAplicacaoProduto(Produto produto, TipoProjeto tipoProjeto) {
        return "Aplicação em " + tipoProjeto.getDescricao().toLowerCase();
    }

    private Projeto convertToEntity(ProjetoDTO dto) {
        Projeto projeto = new Projeto();
        projeto.setNome(dto.getNome());
        projeto.setDescricao(dto.getDescricao());
        projeto.setClienteId(dto.getClienteId());
        projeto.setTipoProjeto(dto.getTipoProjeto());
        projeto.setStatus(dto.getStatus() != null ? dto.getStatus() : StatusProjeto.ORCAMENTO);
        projeto.setDataPrevista(dto.getDataPrevista());
        projeto.setObservacoes(dto.getObservacoes());
        projeto.setMargemLucro(dto.getMargemLucro() != null ? dto.getMargemLucro() : new BigDecimal("20.00"));
        projeto.setUsuarioCriacao(dto.getUsuarioCriacao() != null ? dto.getUsuarioCriacao() : 1);

        projeto.setValorTotal(BigDecimal.ONE);

        if (dto.getMedidas() != null) {
            projeto.setProfundidade(dto.getMedidas().getProfundidade());
            projeto.setLargura(dto.getMedidas().getLargura());
            projeto.setAltura(dto.getMedidas().getAltura());
            projeto.setObservacoesMedidas(dto.getMedidas().getObservacoes());
        }

        return projeto;
    }
}
